package com.harry.clio.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LicenseRefreshRequest(
        @NotBlank(message = "Cần gửi public key")
        @Size(max = 2048, message = "Độ dài public key không hợp lệ")
        String publicKeySpki) {}
