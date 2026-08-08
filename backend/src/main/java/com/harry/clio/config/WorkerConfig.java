package com.harry.clio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WorkerConfig {
    @Value("${clio.book-workers}")
    private int bookWorkers;

    @Bean(value = "bookProcessingExecutor")
    public ThreadPoolTaskExecutor bookProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(bookWorkers);
        executor.setMaxPoolSize(bookWorkers);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("book-processing-");
        executor.initialize();
        return executor;
    }
}
