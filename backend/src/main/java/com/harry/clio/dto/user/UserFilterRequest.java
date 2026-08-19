package com.harry.clio.dto.user;

import jakarta.validation.constraints.Size;

public record UserFilterRequest(
        @Size(max = 255, message = "Độ dài vượt quá mức cho phép")
        String keyword) {}
