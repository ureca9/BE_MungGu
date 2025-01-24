package com.meong9.backend.global.utils;

public final class RedisKeys {

    // 생성자를 private으로 선언하여 인스턴스화 방지
    private RedisKeys() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    // Redis 키 상수 정의
    public static final String PENSION_WEEKLY_VIEW_COUNT = "pension:weekly:viewCount:%s"; // %s는 날짜
    public static final String PLACE_WEEKLY_VIEW_COUNT = "place:%s:weekly:viewCount:%s"; // %s는 날짜
    public static final String TOP_PENSIONS = "top_pensions";
    public static final String TOP_PLACES = "top_places:%s";
    public static final String PENSION_DAILY_VIEW_COUNT = "pension:viewCount:%s"; // %s는 날짜
    public static final String PLACE_DAILY_VIEW_COUNT = "place:%s:viewCount:%s"; // %s는 날짜

    /**
     * 동적으로 완성된 Redis 키를 반환하는 유틸리티 메서드
     *
     * @param pattern Redis 키 패턴 (예: WEEKLY_VIEW_COUNT)
     * @param args    키에 필요한 동적 값들
     * @return 완성된 Redis 키
     */
    public static String format(String pattern, Object... args) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("패턴은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (args == null) {
            throw new IllegalArgumentException("인자는 null일 수 없습니다.");
        }
        return String.format(pattern, args);
    }

    /**
     * 특정 날짜 기반의 펜션 주간 조회수 키를 반환
     *
     * @param date 특정 날짜 (예: "20230118")
     * @return 완성된 주간 조회수 키
     */
    public static String getPensionWeeklyViewCountKey(String date) {
        return format(PENSION_WEEKLY_VIEW_COUNT, date);
    }

    /**
     * 특정 날짜 기반의 시설 주간 조회수 키를 반환
     *
     * @param date 특정 날짜 (예: "20230118")
     * @param categoryName 카테고리 이름 (예: "공원")
     * @return 완성된 주간 조회수 키
     */
    public static String getPlaceWeeklyViewCountKey(String categoryName, String date) {
        return format(PLACE_WEEKLY_VIEW_COUNT, categoryName, date);
    }

    /**
     * 특정 날짜 기반의 펜션 일간 조회수 키를 반환
     *
     * @param date 특정 날짜 (예: "20230118")
     * @return 완성된 주간 조회수 키
     */
    public static String getPensionDailyViewCountKey(String date) {
        return format(PENSION_DAILY_VIEW_COUNT, date);
    }

    /**
     * 특정 날짜 기반의 시설 일간 조회수 키를 반환
     *
     * @param date 특정 날짜 (예: "20230118")
     * @param categoryName 카테고리 이름 (예: "공원")
     * @return 완성된 주간 조회수 키
     */
    public static String getPlaceDailyViewCountKey(String categoryName, String date) {
        return format(PLACE_DAILY_VIEW_COUNT, categoryName, date);
    }

    /**
     *  핫한장소(시설) 키를 반환
     *
     * @param categoryName 카테고리 이름 (예: "공원")
     * @return 완성된 주간 조회수 키
     */
    public static String getTopPlacesKey(String categoryName) {
        return format(TOP_PLACES, categoryName);
    }

}

