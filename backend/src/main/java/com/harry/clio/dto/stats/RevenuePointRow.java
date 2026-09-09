package com.harry.clio.dto.stats;

import java.math.BigDecimal;

public record RevenuePointRow(
        Integer bucket, BigDecimal bookRevenue, BigDecimal subscriptionRevenue) {}
