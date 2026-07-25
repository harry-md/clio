package com.harry.clio.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscriptionPlanRequest(
        @NotNull(message = "Subscription Id không được trống")
        @Positive(message = "Subscription Id không hợp lệ")
        Integer subscriptionPlanId) {}
