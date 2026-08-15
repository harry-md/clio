package com.harry.clio.dto.book;

import com.harry.clio.model.BookAuthorResponse;
import com.harry.clio.model.BookType;

import java.math.BigDecimal;
import java.util.List;

public record BookListResponse(
        Integer id,
        String title,
        BigDecimal price,
        String thumbnail,
        BookType type,
        Double rating,
        Long ratingCount,
        List<BookAuthorResponse> authors) {}
