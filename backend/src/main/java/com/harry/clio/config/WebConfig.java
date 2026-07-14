package com.harry.clio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {
    @Value("${clio.page_size}")
    private int pageSize;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setFallbackPageable(
                    PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
            resolver.setMaxPageSize(100);
            resolver.setOneIndexedParameters(false);
        };
    }
}
