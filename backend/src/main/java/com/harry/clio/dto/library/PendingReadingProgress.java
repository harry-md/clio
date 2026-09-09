package com.harry.clio.dto.library;

import java.time.Instant;

public record PendingReadingProgress(
        int userId, int bookId, String cfiPosition, Instant updatedAt) {
    public static PendingReadingProgress create(int userId, int bookId, String cfiPosition) {
        Instant updatedAt = Instant.now();
        String key = userId + ":" + bookId;

        String value = updatedAt.toEpochMilli() + "|" + cfiPosition;
        return new PendingReadingProgress(userId, bookId, cfiPosition, updatedAt);
    }

    public static PendingReadingProgress toProgress(String key, String value) {
        int fieldSeparator = key.indexOf(":");
        int valueSeparator = value.indexOf("|");

        if (fieldSeparator <= 0 || valueSeparator <= 0) {
            throw new IllegalArgumentException("Progress không hợp lệ.");
        }

        int userId = Integer.parseInt(key.substring(0, fieldSeparator));
        int bookId = Integer.parseInt(key.substring(fieldSeparator + 1));

        long updatedAtMillis = Long.parseLong(value.substring(0, valueSeparator));
        String cfiPosition = value.substring(valueSeparator + 1);

        return new PendingReadingProgress(
                userId, bookId, cfiPosition, Instant.ofEpochMilli(updatedAtMillis));
    }

    public String key() {
        return userId + ":" + bookId;
    }

    public String value() {
        return updatedAt.toEpochMilli() + "|" + cfiPosition;
    }
}
