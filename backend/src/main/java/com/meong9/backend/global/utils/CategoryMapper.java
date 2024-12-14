package com.meong9.backend.global.utils;

import java.util.*;
import java.util.stream.Collectors;

public class CategoryMapper {

    // 기본 카테고리 맵
    private static final Map<String, String> categoryMap = Map.of(
            "1", "공원",
            "2", "관광지",
            "3", "놀이터",
            "4", "카페",
            "5", "해수욕장",
            "6", "마당"
    );

    // 역방향 맵 (카테고리 이름 -> 카테고리 ID)
    private static final Map<String, String> reverseMap = categoryMap.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getValue,  // 카테고리 이름을 키로 사용
                    Map.Entry::getKey,    // 카테고리 ID를 값으로 사용
                    (existing, replacement) -> existing, // 중복 처리 (기존 값 유지)
                    HashMap::new          // HashMap 사용
            ));

    public static String getCategoryName(String categoryId) {
        return categoryMap.getOrDefault(categoryId, "알 수 없음");
    }

    public static String getCategoryId(String categoryName) {
        return reverseMap.getOrDefault(categoryName, "-1");
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
        return reverseMap.containsKey(categoryName);
    }
}
