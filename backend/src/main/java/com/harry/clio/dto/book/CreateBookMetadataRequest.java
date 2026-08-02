package com.harry.clio.dto.book;

import com.harry.clio.entity.BookAuthorResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record CreateBookMetadataRequest(
        @NotBlank(message = "Tên sách không được để trống")
        @Size(max = 255, message = "Tên sách vượt quá độ dài cho phép")
        String title,

        @NotNull(message = "Giá tiền sách không được để trống")
        @DecimalMin(value = "0.0", inclusive = true, message = "Giá tiền sách không được âm")
        @Digits(integer = 13, fraction = 2, message = "Giá tiền sách không hợp lệ")
        BigDecimal price,

        @Valid @NotEmpty(message = "Tác giả không được để trống")
        List<BookAuthorResponse> authors,

        @NotEmpty(message = "Danh mục sách không được để trống")
        Set<@NotNull Integer> categoryIds,

        @Size(max = 20000, message = "Mô tả sách vượt quá độ dài cho phép")
        String description,

        @Size(max = 20, message = "ISBN vượt quá độ dài cho phép")
        String isbn) {}
