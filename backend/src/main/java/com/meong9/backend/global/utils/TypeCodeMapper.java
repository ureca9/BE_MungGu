package com.meong9.backend.global.utils;

import java.util.Map;

public class TypeCodeMapper {
    private static final Map<String, String> typeMap = Map.of(
            "010", "PLACE",
            "020", "PENSION"
    );

    public static String getType(String commonCode) {
        return typeMap.getOrDefault(commonCode, "PENSION");
    }

}
