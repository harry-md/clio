package com.harry.clio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "clio.book-workers")
public record BookWorkerProperties(
        int count, Duration timeout, int maxAttempts, Duration retryDelay) {}
