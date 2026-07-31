package com.harry.clio.dto.book;

import com.harry.clio.entity.BookAuthorResponse;

import java.util.List;

public record SimpleBookResponse(
        Integer id, String title, String thumbnail, List<BookAuthorResponse> authors) {}
