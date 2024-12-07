package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends BaseException {
    static private final String PHOTO_PROCESSING_ERROR = "S3에서 사진 저장 중 문제가 발생했습니다.";
    static private final String WEATHER_API_ERROR = "기상청 api 호출 중 문제가 발생했습니다. %s";

    public InternalServerError(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static InternalServerError photoProcessingError() {
        return new InternalServerError(PHOTO_PROCESSING_ERROR);
    }

    public static InternalServerError weatherApiError(String message){
        return new InternalServerError(String.format(WEATHER_API_ERROR, message));
    }

}
