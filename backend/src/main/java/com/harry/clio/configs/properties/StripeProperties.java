package com.harry.clio.configs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("stripe")
public record StripeProperties() {}
