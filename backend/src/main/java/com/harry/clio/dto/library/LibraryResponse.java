package com.harry.clio.dto.library;

import com.harry.clio.entity.BookAuthorResponse;
import com.harry.clio.entity.UserLibraryType;

import java.util.List;

public record LibraryResponse(
        Integer id,
        UserLibraryType type,
        String cfiPosition,
        Integer bookId,
        String title,
        String thumbnail,
        List<BookAuthorResponse> authors) {}
