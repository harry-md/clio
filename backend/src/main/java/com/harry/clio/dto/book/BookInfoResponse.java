package com.harry.clio.dto.book;

import com.harry.clio.entity.Language;

public record BookInfoResponse(
        String description, Language language, Long fileSize, Long wordCount, String isbn) {}
