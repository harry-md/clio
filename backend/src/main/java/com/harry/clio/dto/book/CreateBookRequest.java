package com.harry.clio.dto.book;

import com.harry.clio.entity.BookType;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateBookRequest(
        @NotBlank(message = "Tên sách không được để trống")
        @Size(max = 255, message = "Tên sách vượt quá độ dài cho phép")
        String title,

        @NotNull(message = "Giá tiền sách không được để trống")
        @Digits(integer = 15, fraction = 2, message = "Giá tiền sách không hợp lệ")
        BigDecimal price,

        BookType type) {}
