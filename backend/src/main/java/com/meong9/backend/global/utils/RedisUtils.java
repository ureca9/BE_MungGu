package com.meong9.backend.global.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class RedisUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static long calculateTTLUntil2AM() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoAM = now.toLocalDate().atStartOfDay().plusDays(1).plusHours(2); // 다음 날 오전 2시
        return Duration.between(now, twoAM).getSeconds();
    }

    // 현재 날짜 포맷팅
    public static String formatCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    // 특정 날짜 포맷팅
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    // n일 전 날짜 포멧팅
    public static String formatRelativeToNowDate(int day){
        return LocalDate.now().minusDays(day).format(DATE_FORMATTER);
    }

}
