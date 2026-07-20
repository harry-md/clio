package com.harry.clio.dto.order;

import java.util.List;

public record StripeSessionInput(Integer orderId, List<StripeBookItem> items) {}
