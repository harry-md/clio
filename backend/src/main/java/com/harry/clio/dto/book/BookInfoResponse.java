package com.harry.clio.dto.book;

import com.harry.clio.model.Language;

public record BookInfoResponse(
        String description, Language language, Long fileSize, Long wordCount, String isbn) {}
