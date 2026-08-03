package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.order.BookPurchaseRequest;
import com.harry.clio.dto.order.StripeCheckoutResponse;
import com.harry.clio.dto.subscription.SubscriptionPlanRequest;
import com.harry.clio.service.OrderService;

import jakarta.validation.Valid;

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
            @Valid @RequestBody BookPurchaseRequest request) {
        return ResponseEntity.ok(orderService.createCheckout(principal.getId(), request));
    }

    @PostMapping("/subscription")
    public ResponseEntity<StripeCheckoutResponse> subscribe(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(
                orderService.createSubscriptionCheckout(principal.getId(), request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload) {
        orderService.handleWebhook(sigHeader, payload);
        return ResponseEntity.ok().build();
    }
}
