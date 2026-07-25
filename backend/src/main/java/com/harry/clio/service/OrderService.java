package com.harry.clio.service;

import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.dto.subscription.SubscriptionPlanRequest;

public interface OrderService {
    StripeCheckoutResponse createCheckout(Integer userId, BookPurchaseRequest request);

    void handleWebhook(String sigHeader, String payload);

    StripeCheckoutResponse createSubscriptionCheckout(
            Integer userId, SubscriptionPlanRequest request);
}
