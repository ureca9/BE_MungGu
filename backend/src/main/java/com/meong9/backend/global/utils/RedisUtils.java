package com.meong9.backend.global.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class RedisUtils {

    public static long calculateTTLUntil2AM() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoAM = now.toLocalDate().atStartOfDay().plusDays(1).plusHours(2); // 다음 날 오전 2시
        return Duration.between(now, twoAM).getSeconds();
    }
}
