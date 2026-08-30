package com.harry.clio.service.impl;

import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.dto.order.StripeLineItem;
import com.harry.clio.dto.order.StripeSessionInput;
import com.harry.clio.dto.subscription.SubscriptionPlanRequest;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.InvalidWebhookException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.model.*;
import com.harry.clio.repository.*;
import com.harry.clio.service.OrderService;
import com.harry.clio.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionAllocationRepository allocationRepository;
    private final RevenueLogRepository revenueLogRepository;

    @Value("${clio.publisher-share}")
    private BigDecimal publisherShare;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Override
    public StripeCheckoutResponse createCheckout(Integer userId, BookPurchaseRequest request) {
        List<Integer> bookIds = request.bookIds();
        Order existingOrder = orderRepository
                .findWithDetailsByUserIdAndBookIdsIn(
                        userId, request.bookIds(), OrderStatus.PENDING, bookIds.size())
                .orElse(null);

        Integer orderId;
        List<StripeLineItem> stripeItems;
        if (existingOrder == null) {
            StripeSessionInput stripeSessionInput =
                    transactionTemplate.execute(status -> createPendingOrder(userId, request));
            orderId = stripeSessionInput.orderId();
            stripeItems = stripeSessionInput.items();
        } else {
            orderId = existingOrder.getId();
            stripeItems = orderDetailRepository.findAllWithItemByOrderId(orderId).stream()
                    .map(d -> new StripeLineItem(
                            d.getBook().getId(), d.getBook().getTitle(), d.getPrice()))
                    .toList();
        }
        Session session = paymentService.createCheckoutSession(orderId, stripeItems);
        return new StripeCheckoutResponse(orderId, session.getUrl());
    }

    private StripeSessionInput createPendingOrder(Integer userId, BookPurchaseRequest request) {
        List<Integer> bookIds = request.bookIds().stream().toList();
        if (userLibraryRepository.existsByUserIdAndBookIdInAndType(
                userId, bookIds, UserLibraryType.PURCHASED)) {
            throw new BadRequestException("Bạn đã sở hữu sách trong danh sách mua!");
        }

        List<Book> books = bookRepository.findAllPurchasableByIdIn(
                bookIds, BookStatus.COMPLETED, BookType.SYSTEM);
        if (books.size() != bookIds.size()) {
            throw new BadRequestException("Có sách không thể mua");
        }

        User user = userRepository.getReferenceById(userId);
        Order order = Order.builder().user(user).status(OrderStatus.PENDING).build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>(books.size());
        List<StripeLineItem> bookItems = new ArrayList<>(books.size());
        for (Book book : books) {
            totalAmount = totalAmount.add(book.getPrice());
            orderDetails.add(OrderDetail.builder()
                    .order(order)
                    .book(book)
                    .price(book.getPrice())
                    .build());
            bookItems.add(new StripeLineItem(book.getId(), book.getTitle(), book.getPrice()));
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);
        return new StripeSessionInput(order.getId(), bookItems);
    }

    @Transactional
    @Override
    public void handleWebhook(String sigHeader, String payload) {
        Event event = paymentService.constructWebhookEvent(sigHeader, payload);
        Session session = getSessionFromEvent(event);

        switch (event.getType()) {
            case "checkout.session.completed" -> handleSucceededPayment(session);
            case "checkout.session.expired" -> handleFailedPayment(session);
        }
    }

    private Session getSessionFromEvent(Event event) {
        StripeObject obj = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new InvalidWebhookException("Lỗi lấy webhook event"));
        if (!(obj instanceof Session session)) {
            throw new InvalidWebhookException("Webhook event không phải là session");
        }
        return session;
    }

    private void handleFailedPayment(Session session) {
        Order order = orderRepository
                .findByIdForUpdate(Integer.parseInt(session.getClientReferenceId()))
                .orElseThrow(() -> new InvalidWebhookException("Không tìm thấy đơn hàng"));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELED);
        }
    }

    private void handleSucceededPayment(Session session) {
        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }

        Order order = orderRepository
                .findByIdForUpdate(Integer.parseInt(session.getClientReferenceId()))
                .orElseThrow(() -> new InvalidWebhookException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        List<OrderDetail> orderDetails =
                orderDetailRepository.findAllWithItemByOrderId(order.getId());
        DetailType type = orderDetails.getFirst().getType();
        switch (type) {
            case BOOK -> handleBookOrder(order, orderDetails);
            case SUBSCRIPTION -> handleSubscriptionOrder(order, orderDetails.getFirst());
        }

        order.setStripeSessionId(session.getId());
        order.setStatus(OrderStatus.PAID);
    }

    private void handleBookOrder(Order order, List<OrderDetail> orderDetails) {
        User user = order.getUser();
        List<UserLibrary> userLibraries = new ArrayList<>(orderDetails.size());

        List<Integer> alreadyInLibBookIds = new ArrayList<>();
        List<RevenueLog> revenueLogs = new ArrayList<>(orderDetails.size());

        for (OrderDetail od : orderDetails) {
            Book book = od.getBook();
            Publisher publisher = book.getPublisher();

            BigDecimal pubRevenue =
                    od.getPrice().multiply(publisherShare).setScale(2, RoundingMode.HALF_UP);
            revenueLogs.add(RevenueLog.builder()
                    .orderDetail(od)
                    .amount(pubRevenue)
                    .owner(RevenueLogOwner.PUBLISHER)
                    .publisher(publisher)
                    .build());
            revenueLogs.add(RevenueLog.builder()
                    .orderDetail(od)
                    .amount(od.getPrice().subtract(pubRevenue))
                    .owner(RevenueLogOwner.PLATFORM)
                    .build());

            alreadyInLibBookIds.add(book.getId());
            userLibraries.add(UserLibrary.builder()
                    .user(user)
                    .book(od.getBook())
                    .type(UserLibraryType.PURCHASED)
                    .build());
        }
        revenueLogRepository.saveAll(revenueLogs);

        List<UserLibrary> existingLibraries =
                userLibraryRepository.findAllByUserIdAndBookIdIn(user.getId(), alreadyInLibBookIds);

        List<Integer> existingBookIds = existingLibraries.stream()
                .map(library -> {
                    if (library.getType() == UserLibraryType.SUBSCRIBED) {
                        library.setType(UserLibraryType.PURCHASED);
                    }
                    return library.getBook().getId();
                })
                .toList();

        userLibraries.removeIf(ul -> existingBookIds.contains(ul.getBook().getId()));
        userLibraryRepository.saveAll(userLibraries);
    }

    private void handleSubscriptionOrder(Order order, OrderDetail orderDetail) {
        LocalDate today = LocalDate.now(ZoneId.of(zoneId));

        SubscriptionPlan plan = orderDetail.getSubscriptionPlan();
        Subscription subscription = Subscription.builder()
                .user(order.getUser())
                .startDate(today)
                .endDate(today.plusMonths(plan.getDuration()))
                .build();
        subscriptionRepository.save(subscription);

        BigDecimal pubRevenue =
                orderDetail.getPrice().multiply(publisherShare).setScale(2, RoundingMode.HALF_UP);

        revenueLogRepository.save(RevenueLog.builder()
                .orderDetail(orderDetail)
                .amount(orderDetail.getPrice().subtract(pubRevenue))
                .owner(RevenueLogOwner.PLATFORM)
                .build());

        allocationRepository.saveAll(allocateSubscription(
                subscription, subscription.getStartDate(), subscription.getEndDate(), pubRevenue));
    }

    private List<SubscriptionAllocation> allocateSubscription(
            Subscription subscription,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal totalAmount) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        List<SubscriptionAllocation> allocations = new ArrayList<>();

        LocalDate current = startDate;
        BigDecimal allocatedAmount = BigDecimal.ZERO;

        while (current.isBefore(endDate)) {
            LocalDate nextMonth = current.withDayOfMonth(1).plusMonths(1);
            LocalDate sliceEnd = nextMonth.isBefore(endDate) ? nextMonth : endDate;

            long elapsedDays = ChronoUnit.DAYS.between(startDate, sliceEnd);

            BigDecimal cumulativeAmount = sliceEnd.equals(endDate)
                    ? totalAmount
                    : totalAmount.multiply(BigDecimal.valueOf(elapsedDays)
                            .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP));

            BigDecimal sliceAmount = cumulativeAmount.subtract(allocatedAmount);

            allocations.add(SubscriptionAllocation.builder()
                    .subscription(subscription)
                    .month(current.getMonthValue())
                    .year(current.getYear())
                    .publisherAmount(sliceAmount)
                    .startAllocateDate(current)
                    .endAllocateDate(sliceEnd)
                    .build());

            allocatedAmount = cumulativeAmount;
            current = sliceEnd;
        }
        return allocations;
    }

    @Override
    public StripeCheckoutResponse createSubscriptionCheckout(
            Integer userId, SubscriptionPlanRequest request) {
        StripeSessionInput input = transactionTemplate.execute(status -> {
            if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
                throw new BadRequestException("Bạn đã có gói đã đăng ký");
            }

            Order existingOrder = orderRepository
                    .findSubOrderWithDetailByUserId(
                            userId, OrderStatus.PENDING, DetailType.SUBSCRIPTION)
                    .orElse(null);

            if (existingOrder == null) {
                SubscriptionPlan plan = subscriptionPlanRepository
                        .findByIdAndActiveTrue(request.planId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Không tìm thấy subscription plan"));
                Order order = Order.builder()
                        .user(userRepository.getReferenceById(userId))
                        .totalAmount(plan.getPrice())
                        .build();
                orderRepository.save(order);

                OrderDetail detail = OrderDetail.builder()
                        .order(order)
                        .subscriptionPlan(plan)
                        .price(plan.getPrice())
                        .type(DetailType.SUBSCRIPTION)
                        .build();
                orderDetailRepository.save(detail);
                return new StripeSessionInput(
                        order.getId(),
                        List.of(new StripeLineItem(plan.getId(), plan.getName(), plan.getPrice())));
            } else {
                OrderDetail detail = existingOrder.getDetails().stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Không tìm thấy chi tiết đơn hàng"));
                SubscriptionPlan plan = detail.getSubscriptionPlan();
                return new StripeSessionInput(
                        existingOrder.getId(),
                        List.of(new StripeLineItem(plan.getId(), plan.getName(), plan.getPrice())));
            }
        });
        Session session = paymentService.createCheckoutSession(input.orderId(), input.items());
        return new StripeCheckoutResponse(input.orderId(), session.getUrl());
    }
}
