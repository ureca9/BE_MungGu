package com.meong9.backend.global.utils;

import java.util.HashMap;
import java.util.Map;

public class WeatherMapper {
    private static final Map<String, String> WEATHER_SKY_CODE_MAP = new HashMap<>();
    private static final Map<String, String> WEATHER_PTY_CODE_MAP = new HashMap<>();

    static {
        // 하늘 상태
        WEATHER_SKY_CODE_MAP.put("1", "맑음");
        WEATHER_SKY_CODE_MAP.put("3", "구름많음");
        WEATHER_SKY_CODE_MAP.put("4", "흐림");

        // 강수 형태
        WEATHER_PTY_CODE_MAP.put("1", "비");
        WEATHER_PTY_CODE_MAP.put("2", "비/눈");
        WEATHER_PTY_CODE_MAP.put("3", "눈");
        WEATHER_PTY_CODE_MAP.put("4", "소나기");
    }
    public static String getWeatherSkyCode(String code) {
        return WEATHER_SKY_CODE_MAP.getOrDefault(code, null);
    }

    public static String getWeatherPtyCode(String code) {
        return WEATHER_PTY_CODE_MAP.getOrDefault(code, null);
    }
}
