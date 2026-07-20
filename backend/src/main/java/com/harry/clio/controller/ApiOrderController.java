package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class ApiOrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<StripeCheckoutResponse> checkout(
            @AuthenticationPrincipal CustomUser principal,
            @RequestBody BookPurchaseRequest request) {
        return ResponseEntity.ok(orderService.createCheckout(principal.getId(), request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload) {
        orderService.handleWebhook(sigHeader, payload);
        return ResponseEntity.ok().build();
    }
}
