package com.harry.clio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "clio.license")
public record LicenseProperties(String key, Duration offlineDuration) {}
