package com.harry.clio.dto.order;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BookPurchaseRequest(
        @NotEmpty(message = "Giỏ hàng trống") List<Integer> bookIds) {}
