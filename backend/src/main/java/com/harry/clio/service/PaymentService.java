package com.harry.clio.service;

import com.harry.clio.dto.order.StripeBookItem;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

import java.util.List;

public interface PaymentService {
    Session createCheckoutSession(Integer orderId, List<StripeBookItem> items);

    Event constructWebhookEvent(String sigHeader, String payload);
}
