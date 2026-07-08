package com.harry.clio.dto.publisher;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PublisherDto(
        Integer userId,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        BigDecimal balance,

        String bankAccountNumber) {}
