package com.harry.clio.infra;

import com.harry.clio.dto.library.PendingReadingProgress;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadingProgressHash {
    private final StringRedisTemplate redisTemplate;

    private static final String READING_PROGRESS = "reading-progress";
    private static final DefaultRedisScript<Long> DELETE_IF_UNCHANGED_SCRIPT =
            new DefaultRedisScript<>("""
                local current = redis.call("HGET", KEYS[1], ARGV[1])

                if current == ARGV[2] then
                    return redis.call("HDEL", KEYS[1], ARGV[1])
                end
                return 0
                """, Long.class);

    public Optional<PendingReadingProgress> get(int userId, int bookId) {
        String key = userId + ":" + bookId;
        Object redisValue = redisTemplate.opsForHash().get(READING_PROGRESS, key);

        if (!(redisValue instanceof String value)) {
            return Optional.empty();
        }
        return Optional.of(PendingReadingProgress.toProgress(key, value));
    }

    public void put(PendingReadingProgress progress) {
        redisTemplate.opsForHash().put(READING_PROGRESS, progress.key(), progress.value());
    }

    public List<PendingReadingProgress> snapshot() {
        Map<Object, Object> progresses = redisTemplate.opsForHash().entries(READING_PROGRESS);

        List<PendingReadingProgress> snapshot = new ArrayList<>(progresses.size());

        for (Map.Entry<Object, Object> e : progresses.entrySet()) {
            String key = String.valueOf(e.getKey());
            String value = String.valueOf(e.getValue());

            snapshot.add(PendingReadingProgress.toProgress(key, value));
        }
        return snapshot;
    }

    public boolean removeIfUnchanged(PendingReadingProgress progress) {
        Long removed = redisTemplate.execute(
                DELETE_IF_UNCHANGED_SCRIPT,
                List.of(READING_PROGRESS),
                progress.key(),
                progress.value());
        return removed == 1;
    }
}
