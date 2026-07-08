package com.harry.clio.dto.book;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.entity.BookAuthorJson;
import com.harry.clio.entity.BookType;

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
        Double rating,
        Long ratingCount,
        List<BookAuthorJson> authors,
        Set<CategoryResponse> categories,
        BookInfoResponse bookInfo,
        Instant createdAt,
        Instant updatedAt) {}
