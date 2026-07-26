package com.harry.clio.dto.order;

import java.math.BigDecimal;

public record StripeLineItem(Integer itemId, String itemName, BigDecimal price) {}
