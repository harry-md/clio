package com.harry.clio.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReadingProgressRequest(
        @NotBlank(message = "CFI position không được để trống")
        @Size(max = 8192, message = "CFI position quá dài")
        String cfiPosition) {}
