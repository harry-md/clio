package com.harry.clio.service;

import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;

public interface OrderService {
    StripeCheckoutResponse createCheckout(Integer userId, BookPurchaseRequest request);

    void handleWebhook(String sigHeader, String payload);
}
