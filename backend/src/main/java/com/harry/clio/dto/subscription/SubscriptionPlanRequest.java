package com.harry.clio.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscriptionPlanRequest(
        @NotNull(message = "Id không được trống") @Positive(message = "Id không hợp lệ")
        Integer planId) {}
