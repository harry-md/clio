package com.harry.clio.service.progress;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public record PendingReadingProgress(
        int userId,
        int bookId,
        String cfiPosition,
        Instant updatedAt,
        String redisField,
        String serializedValue) {

    private static final String FIELD_SEPARATOR = ":";
    private static final String VALUE_SEPARATOR = "|";

    public static PendingReadingProgress create(int userId, int bookId, String cfiPosition) {
        Instant updatedAt = Instant.now();
        String field = createField(userId, bookId);

        String encodedCfi = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(cfiPosition.getBytes(StandardCharsets.UTF_8));

        String value = updatedAt.toEpochMilli() + VALUE_SEPARATOR + encodedCfi;

        return new PendingReadingProgress(userId, bookId, cfiPosition, updatedAt, field, value);
    }

    public static PendingReadingProgress fromRedis(String field, String value) {
        int fieldSeparator = field.indexOf(FIELD_SEPARATOR);
        int valueSeparator = value.indexOf(VALUE_SEPARATOR);

        if (fieldSeparator <= 0 || valueSeparator <= 0) {
            throw new IllegalArgumentException("Reading progress Redis value không hợp lệ");
        }

        int userId = Integer.parseInt(field.substring(0, fieldSeparator));
        int bookId = Integer.parseInt(field.substring(fieldSeparator + 1));

        long updatedAtMillis = Long.parseLong(value.substring(0, valueSeparator));

        String encodedCfi = value.substring(valueSeparator + 1);

        String cfiPosition =
                new String(Base64.getUrlDecoder().decode(encodedCfi), StandardCharsets.UTF_8);

        return new PendingReadingProgress(
                userId, bookId, cfiPosition, Instant.ofEpochMilli(updatedAtMillis), field, value);
    }

    public static String createField(int userId, int bookId) {
        return userId + FIELD_SEPARATOR + bookId;
    }
}
