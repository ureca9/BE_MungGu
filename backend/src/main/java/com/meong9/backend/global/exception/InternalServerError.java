package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends BaseException {
    static private final String PHOTO_PROCESSING_ERROR = "S3에서 사진 저장 중 문제가 발생했습니다.";
    static private final String WEATHER_API_ERROR = "기상청 api 호출 중 문제가 발생했습니다. %s";
    static private final String INVALID_WEATHER_RESPONSE_FORMAT = "기상청 api의 응답이 JSON이 아닙니다. %s";

    static private final String REDIS_MAPPING_ERROR = "Redis 데이터를 매핑하는 데 실패했습니다. %s";
    static private final String SCHEDULER_FAIL_ERROR = "스케쥴링 작업에 실패했습니다. %s";


    public InternalServerError(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static InternalServerError photoProcessingError() {
        return new InternalServerError(PHOTO_PROCESSING_ERROR);
    }

    public static InternalServerError weatherApiError(String message){
        return new InternalServerError(String.format(WEATHER_API_ERROR, message));
    }

    public static InternalServerError invalidWeatherResponseFormat(String entityName) {
        return new InternalServerError(String.format(INVALID_WEATHER_RESPONSE_FORMAT, entityName));
    }

    public static InternalServerError redisMappingError(String entityName) {
        return new InternalServerError(String.format(REDIS_MAPPING_ERROR, entityName));
    }

    public static InternalServerError schedulerFailError(String entityName) {
        return new InternalServerError(String.format(SCHEDULER_FAIL_ERROR, entityName));
    }

}
