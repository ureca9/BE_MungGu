package com.meong9.backend.global.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiterProvider {
    public RateLimiter createSlackRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(1)) // 허용량 초과 시 대기 시간
                .limitRefreshPeriod(Duration.ofSeconds(1)) // 제한 주기
                .limitForPeriod(1) // 주기당 허용 요청 수
                .build();

        return RateLimiter.of("slackRateLimiter", config);
    }
}
