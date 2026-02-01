package com.tradebeyond.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AsyncThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        int cpu = Runtime.getRuntime().availableProcessors();

        return new ThreadPoolExecutor(cpu,
                cpu * 4,
                30, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(5));
    }
}
