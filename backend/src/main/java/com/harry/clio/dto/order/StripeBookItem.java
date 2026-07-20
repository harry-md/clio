package com.harry.clio.dto.order;

import java.math.BigDecimal;

public record StripeBookItem(Integer bookId, String bookTitle, BigDecimal price) {}
