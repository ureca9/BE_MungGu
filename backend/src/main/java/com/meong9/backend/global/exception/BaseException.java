package com.meong9.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class BaseException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public BaseException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
