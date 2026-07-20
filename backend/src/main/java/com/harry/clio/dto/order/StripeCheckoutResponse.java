package com.harry.clio.dto.order;

public record StripeCheckoutResponse(Integer orderId, String checkoutUrl) {}
