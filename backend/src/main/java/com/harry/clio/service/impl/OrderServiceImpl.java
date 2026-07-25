package com.harry.clio.service.impl;

import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.dto.order.StripeLineItem;
import com.harry.clio.dto.order.StripeSessionInput;
import com.harry.clio.dto.subscription.SubscriptionPlanRequest;
import com.harry.clio.entity.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.InvalidWebhookException;
import com.harry.clio.repository.*;
import com.harry.clio.service.OrderService;
import com.harry.clio.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final PaymentService paymentService;
    private final PublisherRepository publisherRepository;
    private final TransactionTemplate transactionTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private static final BigDecimal PUBLISHER_SHARE = new BigDecimal(0.7);

    @Override
    public StripeCheckoutResponse createCheckout(Integer userId, BookPurchaseRequest request) {
        List<Integer> bookIds = request.bookIds();
        Order existingOrder = orderRepository
                .findWithOrderDetailsByUserIdAndBookIdsIn(userId, request.bookIds(), bookIds.size())
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
        if (userLibraryRepository.existsByUserIdAndBookIdIn(userId, bookIds)) {
            throw new BadRequestException("Bạn đã sở hữu sách trong danh sách mua!");
        }

        List<Book> books = bookRepository.findAllPurchasableByIdIn(bookIds);
        if (books.size() != bookIds.size()) {
            throw new BadRequestException("Có sách không thể mua!");
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
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = getSessionFromEvent(event);
                handleSucceededPayment(session);
            }
            case "checkout.session.expired" -> {
                Session session = getSessionFromEvent(event);
                handleFailedPayment(session);
            }
            default -> {}
        }
    }

    private Session getSessionFromEvent(Event event) {
        StripeObject obj = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new InvalidWebhookException("Lỗi deserialize webhook event"));
        if (!(obj instanceof Session session)) {
            throw new InvalidWebhookException("Webhook event không phải là session");
        }
        return session;
    }

    private void handleSucceededPayment(Session session) {
        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }
        Integer orderId = Integer.parseInt(session.getClientReferenceId());
        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() -> new InvalidWebhookException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        List<OrderDetail> orderDetails =
                orderDetailRepository.findAllWithItemByOrderId(order.getId());
        List<UserLibrary> userLibraries = new ArrayList<>(orderDetails.size());

        Map<Integer, BigDecimal> revenueByPublisher = new HashMap<>();
        for (OrderDetail od : orderDetails) {
            Book book = od.getBook();
            Publisher publisher = book.getPublisher();
            BigDecimal publisherRevenue =
                    od.getPrice().multiply(PUBLISHER_SHARE).setScale(2, RoundingMode.HALF_UP);
            revenueByPublisher.merge(publisher.getUserId(), publisherRevenue, BigDecimal::add);

            userLibraries.add(UserLibrary.builder()
                    .user(order.getUser())
                    .book(od.getBook())
                    .type(UserLibraryType.PURCHASED)
                    .build());
        }

        revenueByPublisher.forEach((publisherId, amount) -> {
            int result = publisherRepository.increaseBalance(publisherId, amount);
            if (result != 1) {
                throw new RuntimeException("Lỗi chỉnh sửa số dư cho publisher");
            }
        });

        userLibraryRepository.saveAll(userLibraries);
        order.setStripeSessionId(session.getId());
        order.setStatus(OrderStatus.PAID);
    }

    private void handleFailedPayment(Session session) {
        Order order = orderRepository
                .findByIdForUpdate(Integer.parseInt(session.getClientReferenceId()))
                .orElseThrow(() -> new InvalidWebhookException("Không tìm thấy đơn hàng"));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELED);
        }
    }

    @Override
    public StripeCheckoutResponse createSubscriptionCheckout(
            Integer userId, SubscriptionPlanRequest request) {
        StripeSessionInput input = transactionTemplate.execute(status -> {
            rejectActiveSubscription(userId);

            Order existingOrder =
                    orderRepository.findSubOrderWithOrderDetailByUserId(userId).orElse(null);
            if (existingOrder == null) {
                SubscriptionPlan plan = subscriptionPlanRepository
                        .findByIdAndActiveTrue(request.subscriptionPlanId())
                        .orElseThrow(
                                () -> new BadRequestException("Không tìm thấy subscription plan"));
                Order order = Order.builder()
                        .user(userRepository.getReferenceById(userId))
                        .totalAmount(plan.getPrice())
                        .build();
                orderRepository.save(order);

                OrderDetail detail = OrderDetail.builder()
                        .order(order)
                        .subscriptionPlan(plan)
                        .price(plan.getPrice())
                        .type(OrderDetailType.SUBSCRIPTION)
                        .build();
                orderDetailRepository.save(detail);
                return new StripeSessionInput(
                        order.getId(),
                        List.of(new StripeLineItem(plan.getId(), plan.getName(), plan.getPrice())));
            } else {
                OrderDetail detail = existingOrder.getDetails().stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new BadRequestException("Không tìm thấy chi tiết đơn hàng"));
                SubscriptionPlan plan = detail.getSubscriptionPlan();
                return new StripeSessionInput(
                        existingOrder.getId(),
                        List.of(new StripeLineItem(plan.getId(), plan.getName(), plan.getPrice())));
            }
        });
        Session session = paymentService.createCheckoutSession(input.orderId(), input.items());
        return new StripeCheckoutResponse(input.orderId(), session.getUrl());
    }

    private void rejectActiveSubscription(Integer userId) {
        if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Bạn đã có subscription đang active");
        }
    }
}
