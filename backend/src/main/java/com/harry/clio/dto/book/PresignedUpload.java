package com.harry.clio.dto.book;

public record PresignedUpload(String objectKey, String uploadUrl, String contentType) {}
