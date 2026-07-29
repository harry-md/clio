package com.harry.clio.controller;

import com.harry.clio.dto.subscription.SubscriptionPlanResponse;
import com.harry.clio.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/subscription-plans")
public class ApiSubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResponse>> list() {
        return ResponseEntity.ok(subscriptionPlanService.getSubscriptionPlans());
    }
}
