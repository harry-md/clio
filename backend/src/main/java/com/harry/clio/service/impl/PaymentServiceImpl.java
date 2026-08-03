package com.harry.clio.service.impl;

import com.harry.clio.dto.order.StripeLineItem;
import com.harry.clio.exception.InvalidWebhookException;
import com.harry.clio.exception.PaymentException;
import com.harry.clio.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${stripe.success-url}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel-url}")
    private String stripeCancelUrl;

    @Override
    public Session createCheckoutSession(Integer orderId, List<StripeLineItem> items) {
        try {
            List<SessionCreateParams.LineItem> lineItems =
                    items.stream().map(this::createLineItem).toList();
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeSuccessUrl)
                    .setCancelUrl(stripeCancelUrl)
                    .setClientReferenceId(orderId.toString())
                    .putMetadata("orderId", orderId.toString())
                    .addAllLineItem(lineItems)
                    .build();
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(stripeSecretKey)
                    .setIdempotencyKey(orderId.toString())
                    .build();
            return Session.create(params, requestOptions);
        } catch (StripeException ex) {
            throw new PaymentException("Không thể tạo phiên thanh toán", ex);
        }
    }

    private SessionCreateParams.LineItem createLineItem(StripeLineItem item) {
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(item.itemName())
                        .putMetadata("itemId", item.itemId().toString())
                        .build();
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("vnd")
                        .setUnitAmount(item.price().longValueExact())
                        .setProductData(productData)
                        .build();
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build();
    }

    @Override
    public Event constructWebhookEvent(String sigHeader, String payload) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new InvalidWebhookException("Webhook không hợp lệ");
        }
    }
}
