package com.harry.clio.dto.book;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BookFilterRequest(
        @Size(max = 255, message = "Tên sách không được vượt quá độ dài cho phép")
        String title,

        @Size(max = 255, message = "Tên tác giả không được vượt quá độ dài cho phép")
        String authorFullname,

        @PositiveOrZero(message = "Khoảng giá sách bắt đầu không hợp lệ")
        @Digits(integer = 13, fraction = 2, message = "Khoảng giá sách bắt đầu không hợp lệ")
        BigDecimal fromPrice,

        @PositiveOrZero(message = "Khoảng giá sách kết thúc không hợp lệ")
        @Digits(integer = 13, fraction = 2, message = "Khoảng giá sách kết thúc không hợp lệ")
        BigDecimal toPrice,

        @PositiveOrZero(message = "Khoảng điểm của sách bắt đầu không hợp lệ")
        @Max(value = 5, message = "Khoảng điểm của sách bắt đầu không hợp lệ")
        Integer fromRating,

        @PositiveOrZero(message = "Khoảng điểm đánh giá của sách kết thúc không hợp lệ")
        @Max(value = 5, message = "Khoảng điểm đánh giá của sách kết thúc không hợp lệ")
        Integer toRating) {

    @AssertTrue(message = "Khoảng giá sách không hợp lệ")
    public boolean isValidPriceRange() {
        return fromPrice == null || toPrice == null || fromPrice.compareTo(toPrice) <= 0;
    }

    @AssertTrue(message = "Khoảng điểm đánh giá của sách không hợp lệ")
    public boolean isValidRatingRange() {
        return fromRating == null || toRating == null || fromRating <= toRating;
    }
}
