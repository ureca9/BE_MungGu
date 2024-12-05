package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    static private final String INVALID_IMAGE_VIDEO_FORMAT = "지원하는 이미지 형식이 아닙니다.";
    static private final String INVALID_PUPPYID_FORMAT = "유효하지 않은 품종 ID 입니다.";
    static private final String INVALID_FILE_FORMAT = "%s는 유효하지 않은 FileKey 입니다.";

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static BadRequestException invalidImageVideoFormat() {
        return new BadRequestException(INVALID_IMAGE_VIDEO_FORMAT);
    }

    public static BadRequestException invalidPuppyIdFormat() {
        return new BadRequestException(INVALID_PUPPYID_FORMAT);
    }

    public static BadRequestException invalidFilekeyFormat(String entityName) {
        return new BadRequestException(String.format(INVALID_FILE_FORMAT, entityName));
    }
}
