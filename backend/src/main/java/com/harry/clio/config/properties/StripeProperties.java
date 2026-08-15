package com.harry.clio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("stripe")
public record StripeProperties(
        String secretKey, String webhookSecret, String successUrl, String cancelUrl) {}
