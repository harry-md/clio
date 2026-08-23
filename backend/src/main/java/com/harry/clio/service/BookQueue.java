package com.harry.clio.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookQueue {
    private static final String READY_QUEUE_KEY = "book-ready-queue";
    private static final String PROCESS_QUEUE_KEY = "book-process-queue";

    private final StringRedisTemplate redisTemplate;

    public void enqueue(Integer bookId) {
        redisTemplate.opsForList().leftPush(READY_QUEUE_KEY, bookId.toString());
    }

    public Optional<Integer> claim(Duration timeout) {
        String bookId = redisTemplate
                .opsForList()
                .move(
                        READY_QUEUE_KEY,
                        RedisListCommands.Direction.RIGHT,
                        PROCESS_QUEUE_KEY,
                        RedisListCommands.Direction.LEFT,
                        timeout);

        if (bookId == null) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(bookId));
    }

    public boolean acknowledge(int bookId) {
        return redisTemplate.opsForList().remove(PROCESS_QUEUE_KEY, 1, String.valueOf(bookId)) == 1;
    }

    public int recoverProcessingJobs() {
        int jobs = 0;
        while (true) {
            String bookId = redisTemplate
                    .opsForList()
                    .move(
                            PROCESS_QUEUE_KEY,
                            RedisListCommands.Direction.LEFT,
                            READY_QUEUE_KEY,
                            RedisListCommands.Direction.RIGHT);

            if (bookId == null) {
                return jobs;
            }
            jobs++;
        }
    }
}
