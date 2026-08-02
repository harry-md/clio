package com.harry.clio.dto.library;

import com.harry.clio.dto.book.SimpleBookResponse;
import com.harry.clio.entity.UserLibraryType;

public record LibraryResponse(
        Integer id, UserLibraryType type, String cfiPosition, SimpleBookResponse book) {}
