package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends BaseException {
    static private final String PHOTO_PROCESSING_ERROR = "사진 처리 중 문제가 발생했습니다.";

    public InternalServerError(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static InternalServerError photoProcessingError() {
        return new InternalServerError(PHOTO_PROCESSING_ERROR);
    }
}
