package com.harry.clio.dto.book;

import com.harry.clio.model.BookAuthorInfo;
import com.harry.clio.model.BookStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminBookListResponse(
        Integer id,
        String title,
        BigDecimal price,
        String thumbnail,
        Double rating,
        Long ratingCount,
        List<BookAuthorInfo> authors,
        BookStatus status,
        boolean active,
        Instant createdAt) {}
