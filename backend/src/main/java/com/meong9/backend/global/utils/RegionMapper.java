package com.meong9.backend.global.utils;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionMapper {

    private static final Map<String, String> WEATHER_REGION_CODE_MAP = new HashMap<>();
    private static final Map<String, String> REGION_NAME_MAP = new HashMap<>();
    private static final Map<String, String[]> WEATHER_REGION_XY_MAP = new HashMap<>();

    private static final Map<String, List<String>> REGION_MAPPING = new HashMap<>();

    static {
        WEATHER_REGION_CODE_MAP.put("서울", "11B00000");
        WEATHER_REGION_CODE_MAP.put("경기", "11B00000");
        WEATHER_REGION_CODE_MAP.put("인천", "11B00000");
        WEATHER_REGION_CODE_MAP.put("강원", "11D10000");
        WEATHER_REGION_CODE_MAP.put("충청", "11C20000");
        WEATHER_REGION_CODE_MAP.put("전라", "11F20000");
        WEATHER_REGION_CODE_MAP.put("경상", "11H20000");
        WEATHER_REGION_CODE_MAP.put("제주", "11G00000");

        REGION_NAME_MAP.put("서울", "SEOUL");
        REGION_NAME_MAP.put("경기", "GYEONGGI");
        REGION_NAME_MAP.put("인천", "INCHEON");
        REGION_NAME_MAP.put("강원", "GANGWON");
        REGION_NAME_MAP.put("충청", "CHUNGCHEONG");
        REGION_NAME_MAP.put("전라", "JEOLLA");
        REGION_NAME_MAP.put("경상", "GYEONGSANG");
        REGION_NAME_MAP.put("제주", "JEJU");

        WEATHER_REGION_XY_MAP.put("서울", new String[]{"60", "127"});
        WEATHER_REGION_XY_MAP.put("경기", new String[]{"60", "120"});
        WEATHER_REGION_XY_MAP.put("인천", new String[]{"55", "124"});
        WEATHER_REGION_XY_MAP.put("강원", new String[]{"93", "132"});
        WEATHER_REGION_XY_MAP.put("충청", new String[]{"67", "100"});
        WEATHER_REGION_XY_MAP.put("전라", new String[]{"60", "74"});
        WEATHER_REGION_XY_MAP.put("경상", new String[]{"89", "90"});
        WEATHER_REGION_XY_MAP.put("제주", new String[]{"53", "38"});

        REGION_MAPPING.put("충청", List.of("충청", "대전", "세종"));
        REGION_MAPPING.put("전라", List.of("전라", "광주", "전북"));
        REGION_MAPPING.put("경상", List.of("경상", "울산", "부산", "대구"));
        REGION_MAPPING.put("서울", List.of("서울"));
        REGION_MAPPING.put("강원", List.of("강원"));
        REGION_MAPPING.put("인천", List.of("인천"));
        REGION_MAPPING.put("경기", List.of("경기"));
        REGION_MAPPING.put("제주", List.of("제주"));
    }

    public static String getWeatherRegion(String regionName) {
        return WEATHER_REGION_CODE_MAP.getOrDefault(regionName, null);
    }

    public static String[] getWeatherXYRegion(String regionName) {
        return WEATHER_REGION_XY_MAP.getOrDefault(regionName, null);
    }

    public static String getRegionEng(String regionName) {
        return REGION_NAME_MAP.getOrDefault(regionName, null);
    }

    public static Map<String, String> getWeatherRegionAll(){
        return Collections.unmodifiableMap(WEATHER_REGION_CODE_MAP); // 읽기 전용
    }

    public static Map<String, List<String>> getRegionMapping() {
        return Collections.unmodifiableMap(REGION_MAPPING); // 읽기 전용
    }

}