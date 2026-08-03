package com.harry.clio.dto.library;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LibraryRequest(
        @NotNull(message = "Phải gửi bookId") @Positive(message = "Id không hợp lệ")
        Integer bookId) {}
