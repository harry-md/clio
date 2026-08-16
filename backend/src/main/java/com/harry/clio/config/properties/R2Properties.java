package com.harry.clio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String bucketName, String accountId, String accessKey, String secretKey) {}
