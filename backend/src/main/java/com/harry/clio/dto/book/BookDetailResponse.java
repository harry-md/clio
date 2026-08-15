package com.harry.clio.dto.book;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.model.BookAuthorResponse;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record BookDetailResponse(
        Integer id,
        String title,
        BigDecimal price,
        String thumbnail,
        BookType type,
        BookStatus status,
        Double rating,
        Long ratingCount,
        List<BookAuthorResponse> authors,
        Set<CategoryResponse> categories,
        BookInfoResponse bookInfo,
        Instant createdAt,
        Instant updatedAt) {}
