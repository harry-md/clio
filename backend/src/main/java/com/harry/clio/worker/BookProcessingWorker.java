package com.harry.clio.worker;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookProcessingWorker {
    private static final String QUEUE_KEY = "clio:book-processing-queue";
    private final StringRedisTemplate redisTemplate;
}
