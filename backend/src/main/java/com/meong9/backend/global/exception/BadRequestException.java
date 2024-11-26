package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    static private final String INVALID_IMAGE_FORMAT= "지원하는 이미지 형식이 아닙니다.";

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static BadRequestException invalidImageFormat() {
        return new BadRequestException(INVALID_IMAGE_FORMAT);
    }
}
