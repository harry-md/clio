package com.harry.clio.service.progress;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReadingProgressBuffer {
    private static final String PENDING_KEY = "clio:reading-progress:pending";
    private static final DefaultRedisScript<Long> DELETE_IF_UNCHANGED_SCRIPT =
            new DefaultRedisScript<>("""
                local current = redis.call("HGET", KEYS[1], ARGV[1])

                if current == ARGV[2] then
                    return redis.call("HDEL", KEYS[1], ARGV[1])
                end

                return 0
                """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public void put(PendingReadingProgress progress) {
        redisTemplate
                .opsForHash()
                .put(PENDING_KEY, progress.redisField(), progress.serializedValue());
    }

    public Optional<PendingReadingProgress> find(int userId, int bookId) {

        String field = PendingReadingProgress.createField(userId, bookId);

        Object value = redisTemplate.opsForHash().get(PENDING_KEY, field);

        if (!(value instanceof String serializedValue)) {
            return Optional.empty();
        }

        return Optional.of(PendingReadingProgress.fromRedis(field, serializedValue));
    }

    public List<PendingReadingProgress> snapshot() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(PENDING_KEY);

        List<PendingReadingProgress> result = new ArrayList<>(entries.size());

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());

            result.add(PendingReadingProgress.fromRedis(field, value));
        }

        return result;
    }

    public boolean removeIfUnchanged(PendingReadingProgress progress) {

        Long removed = redisTemplate.execute(
                DELETE_IF_UNCHANGED_SCRIPT,
                List.of(PENDING_KEY),
                progress.redisField(),
                progress.serializedValue());

        return removed != null && removed == 1L;
    }
}
