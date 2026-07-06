package com.harry.clio.dto;

import com.harry.clio.entity.BookAuthorJson;
import com.harry.clio.entity.BookType;

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
        List<BookAuthorJson> authors) {}
