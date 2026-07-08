package com.harry.clio.service;

import com.harry.clio.config.RedisKeys;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;

@RequiredArgsConstructor
@Service
public class BookProcessingQueue {
    private final StringRedisTemplate redisTemplate;

    public void enqueue(Integer bookId) {
        try {
            redisTemplate.opsForList().leftPush(RedisKeys.BOOK_PROCESSING_QUEUE, bookId.toString());
        } catch (JacksonException ex) {
            throw new RuntimeException("Lỗi tạo job xử lý ebook", ex);
        }
    }
}
