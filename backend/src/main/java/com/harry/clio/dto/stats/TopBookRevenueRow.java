package com.harry.clio.dto.stats;

import java.math.BigDecimal;

public record TopBookRevenueRow(Integer bookId, String title, Long sales, BigDecimal revenue) {}
