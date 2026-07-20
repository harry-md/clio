package com.harry.clio.service.impl;

import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeBookItem;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.dto.order.StripeSessionInput;
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
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public StripeCheckoutResponse createCheckout(Integer userId, BookPurchaseRequest request) {
        List<Integer> bookIds = request.bookIds();
        Order existingOrder = orderRepository
                .findWithDetailsByUserIdAndBookIdsIn(userId, request.bookIds(), bookIds.size())
                .orElse(null);

        Integer orderId;
        List<StripeBookItem> stripeItems;
        if (existingOrder == null) {
            StripeSessionInput stripeSessionInput =
                    transactionTemplate.execute(status -> createPendingOrder(userId, request));
            orderId = stripeSessionInput.orderId();
            stripeItems = stripeSessionInput.items();
        } else {
            orderId = existingOrder.getId();
            stripeItems = orderDetailRepository.findAllByOrderId(orderId).stream()
                    .map(d ->
                            new StripeBookItem(d.getBook().getId(), d.getBookTitle(), d.getPrice()))
                    .toList();
        }
        Session session = paymentService.createCheckoutSession(orderId, stripeItems);
        return new StripeCheckoutResponse(orderId, session.getUrl());
    }

    private StripeSessionInput createPendingOrder(Integer userId, BookPurchaseRequest request) {
        List<Integer> bookIds = request.bookIds().stream().toList();
        if (userBookRepository.existsByUserIdAndBookIdIn(userId, bookIds)) {
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
        List<StripeBookItem> bookItems = new ArrayList<>(books.size());
        for (Book book : books) {
            totalAmount = totalAmount.add(book.getPrice());
            orderDetails.add(OrderDetail.builder()
                    .order(order)
                    .book(book)
                    .bookTitle(book.getTitle())
                    .price(book.getPrice())
                    .build());
            bookItems.add(new StripeBookItem(book.getId(), book.getTitle(), book.getPrice()));
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
                handlePaymentSucceeded(session);
            }
            case "checkout.session.expired" -> {
                Session session = getSessionFromEvent(event);
                handlePaymentFailed(session);
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

    private void handlePaymentSucceeded(Session session) {
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
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderId(order.getId());
        List<UserBook> userBooks = orderDetails.stream()
                .map(od -> UserBook.builder()
                        .user(order.getUser())
                        .book(od.getBook())
                        .type(UserBookType.PURCHASED)
                        .build())
                .toList();
        userBookRepository.saveAll(userBooks);
        order.setStripeSessionId(session.getId());
        order.setStatus(OrderStatus.PAID);
    }

    private void handlePaymentFailed(Session session) {
        Order order = orderRepository
                .findByIdForUpdate(Integer.parseInt(session.getClientReferenceId()))
                .orElseThrow(() -> new InvalidWebhookException("Không tìm thấy đơn hàng"));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELED);
        }
    }
}
