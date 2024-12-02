package com.meong9.backend.global.utils;

import java.util.Map;

public class EnterPetSizeMapper {
    private static final Map<String, String> enterPetSizeMap = Map.of(
            "010", "소형견 가능",
            "020", "중형견 가능",
            "030", "대형견 가능"
    );

    public static String getEnterPetSize(String commonCode) {
        return enterPetSizeMap.getOrDefault(commonCode, "대형견 가능");
    }

}
