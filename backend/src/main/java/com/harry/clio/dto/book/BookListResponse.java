package com.harry.clio.dto.book;

import com.harry.clio.model.BookAuthorInfo;
import com.harry.clio.model.BookType;

import java.io.Serializable;
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
        List<BookAuthorInfo> authors)
        implements Serializable {}
