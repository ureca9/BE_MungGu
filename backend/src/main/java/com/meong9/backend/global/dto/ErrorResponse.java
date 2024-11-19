package com.meong9.backend.global.dto;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ErrorResponse {

    private HttpStatusCode status;
    private String message;
    private String timestamp;

    public static ErrorResponse error(HttpStatusCode status, String message) {
        return new ErrorResponse(status, message);
    }

    public ErrorResponse(HttpStatusCode status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = getCurrentTimestamp();
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
