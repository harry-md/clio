package com.harry.clio.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookQueue {
    private static final String QUEUE_KEY = "book-process-queue";
    private final StringRedisTemplate redisTemplate;

    public void enqueue(Integer bookId) {
        redisTemplate.opsForList().leftPush(QUEUE_KEY, bookId.toString());
    }

    public Optional<Integer> dequeue(Duration timeout) {
        String result = redisTemplate.opsForList().rightPop(QUEUE_KEY, timeout);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(result));
    }
}
