package com.meong9.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
@EnableRetry
public class AsyncConfig {
    @Bean("videoTaskExecutor")
    public ThreadPoolTaskExecutor videoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 현재 머신의 논리 코어 수 조회 (8코어 환경이라면 8)
        int cores = Runtime.getRuntime().availableProcessors();
        // CPU 바운드 작업이므로, 코어 수 - 1 만큼만 스레드를 생성
        int threads = Math.max(1, cores - 1);

        executor.setCorePoolSize(threads);
        executor.setMaxPoolSize(threads);
        // 큐는 스레드 수 * 3 정도가 적당
        executor.setQueueCapacity(threads * 3);
        executor.setThreadNamePrefix("VideoEncoder-");
        executor.initialize();
        return executor;
    }
}
