package com.meong9.backend.global.utils;

import java.util.Map;

public class RoleCodeMapper {

    private static final Map<String, String> roleMap = Map.of(
            "010", "MEMBER",
            "020", "ADMIN"
    );

    public static String getRole(String commonCode) {
        return roleMap.getOrDefault(commonCode, "MEMBER");
    }

}
