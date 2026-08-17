package com.harry.clio.dto.book;

import com.harry.clio.model.Language;

import java.io.Serializable;

public record BookInfoResponse(
        String description, Language language, Long fileSize, Long wordCount, String isbn)
        implements Serializable {}
