package com.meong9.backend.global.entity;

/**
 * 장소 유형을 나타내는 코드 열거형.
 * 010: 일반 장소
 * 020: 펜션
 */
public enum PLACE_PEN_TYPE_CODE {
    PLACE("010", "place"),
    PENSION("020", "pension");

    private final String code;
    private final String description;

    PLACE_PEN_TYPE_CODE(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 코드 값으로 PLACE_TYPE_CODE를 반환하는 유틸리티 메서드.
     *
     * @param code 코드 값
     * @return 대응하는 PLACE_TYPE_CODE
     * @throws IllegalArgumentException 잘못된 코드 값일 경우
     */
    public static PLACE_PEN_TYPE_CODE fromCode(String code) {
        for (PLACE_PEN_TYPE_CODE type : PLACE_PEN_TYPE_CODE.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
