package com.harry.clio.dto.subscription;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(
        Integer id, String name, BigDecimal price, Integer duration, boolean active) {}
