package com.harry.clio.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Min(value = 1, message = "Điểm đánh giá không hợp lệ")
        @Max(value = 5, message = "Điểm đánh giá không hợp lệ")
        int rating,

        @Size(max = 500, message = "Độ dài không hợp lệ") String comment) {}
