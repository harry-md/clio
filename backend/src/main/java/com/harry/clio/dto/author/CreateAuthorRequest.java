package com.harry.clio.dto.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorRequest(
        @NotBlank(message = "Tên tác giả không được để trống")
        @Size(max = 255, message = "Tên tác giả vượt quá độ dài cho phép")
        String fullName,

        @Size(max = 20000, message = "Tiểu sử vượt quá độ dài cho phép")
        String biography) {}
