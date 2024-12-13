package com.meong9.backend.global.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryMapper {

    private static final Map<String, String> categoryMap = Map.of(
            "1", "공원",
            "2", "관광지",
            "3", "놀이터",
            "4", "카페",
            "5", "해수욕장",
            "6", "마당"
    );

    public static String getCategoryName(String categoryId) {
        return categoryMap.getOrDefault(categoryId, "알 수 없음");
    }

    public static String getCategoryId(String categoryName) {
        return categoryMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(categoryName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("-1");
    }

    /**
     * 모든 카테고리 이름 목록 반환
     */
    public static List<String> getAllCategoryNames() {
        return new ArrayList<>(categoryMap.values());
    }

    /**
     * 모든 카테고리 ID 목록 반환
     */
    public static List<String> getAllCategoryIds() {
        return new ArrayList<>(categoryMap.keySet());
    }

    /**
     * 주어진 카테고리 ID가 유효한지 확인
     */
    public static boolean isValidCategoryId(String categoryId) {
        return categoryMap.containsKey(categoryId);
    }

    /**
     * 주어진 카테고리 이름이 유효한지 확인
     */
    public static boolean isValidCategoryName(String categoryName) {
        return categoryMap.containsValue(categoryName);
    }
}
