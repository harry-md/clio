package com.harry.clio.dto.book;

import java.time.Instant;

public record DownloadResponse(String downloadUrl, Instant urlExpiredAt, String license) {}
