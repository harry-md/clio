package com.harry.clio.dto.book;

import com.harry.clio.entity.Language;

public record BookInfoResponse(
        String description, Language language, Long fileSizeBytes, Long wordCount, String isbn) {}
