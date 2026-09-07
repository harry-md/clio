package com.harry.clio.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Tên thể loại không được để trống")
        @Size(max = 255, message = "Tên thể loại vượt quá kích thước cho phép")
        String name) {}
