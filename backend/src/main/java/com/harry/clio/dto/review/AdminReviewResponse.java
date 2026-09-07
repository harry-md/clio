package com.harry.clio.dto.review;

public record AdminReviewResponse(
        Integer id,
        Integer bookId,
        String bookTitle,
        Integer userId,
        String username,
        int rating,
        String comment) {}
