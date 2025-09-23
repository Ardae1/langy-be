package com.example.languageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution. While Virtual Threads handle
 * I/O-bound tasks efficiently, a separate thread pool can still be beneficial
 * for CPU-intensive tasks or when you need explicit control over background
 * processing.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Adjust as needed
        executor.setMaxPoolSize(10);  // Adjust as needed
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-Task-");
        executor.initialize();
        return executor;
    }
}
