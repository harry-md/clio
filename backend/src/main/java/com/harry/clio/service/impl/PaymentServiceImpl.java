package com.harry.clio.service.impl;

import com.harry.clio.config.properties.StripeProperties;
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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final StripeProperties stripeProps;

    @Override
    public Session createCheckoutSession(Integer orderId, List<StripeLineItem> items) {
        try {
            List<SessionCreateParams.LineItem> lineItems =
                    items.stream().map(this::createLineItem).toList();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeProps.successUrl())
                    .setCancelUrl(stripeProps.cancelUrl())
                    .setExpiresAt((System.currentTimeMillis() / 1000) + 1800)
                    .setClientReferenceId(orderId.toString())
                    .putMetadata("orderId", orderId.toString())
                    .addAllLineItem(lineItems)
                    .build();

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(stripeProps.secretKey())
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
            return Webhook.constructEvent(payload, sigHeader, stripeProps.webhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new InvalidWebhookException("Webhook không hợp lệ");
        }
    }
}
