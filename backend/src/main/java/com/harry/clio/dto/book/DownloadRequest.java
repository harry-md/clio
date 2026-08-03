package com.harry.clio.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DownloadRequest(
        @NotNull(message = "Cần gửi bookId lên để tải sách") @Positive(message = "Id không hợp lệ")
        Integer bookId,

        @NotBlank(message = "Cần gửi public key lên để tải sách")
        @Size(max = 2048, message = "Độ dài không hợp lệ")
        String publicKeySpki) {}
