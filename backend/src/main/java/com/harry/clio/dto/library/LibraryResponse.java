package com.harry.clio.dto.library;

import com.harry.clio.model.BookAuthorInfo;
import com.harry.clio.model.UserLibraryType;

import java.util.List;

public record LibraryResponse(
        Integer id,
        UserLibraryType type,
        String cfiPosition,
        Integer bookId,
        String title,
        String thumbnail,
        List<BookAuthorInfo> authors) {}
