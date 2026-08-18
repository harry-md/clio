package com.harry.clio.dto.publisher;

import java.math.BigDecimal;

public record AdminPublisherDto(
        Integer userId,
        String username,
        String firstName,
        String lastName,
        String email,
        String avatar,
        String bankAccountNumber,
        BigDecimal balance) {}
