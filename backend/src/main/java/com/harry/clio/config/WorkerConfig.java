package com.harry.clio.config;

import com.harry.clio.config.properties.BookWorkerProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class WorkerConfig {
    private final BookWorkerProperties workerProps;

    @Bean
    public ThreadPoolTaskExecutor bookExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(workerProps.count());
        executor.setMaxPoolSize(workerProps.count());
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("book-worker-");
        executor.initialize();
        return executor;
    }
}
