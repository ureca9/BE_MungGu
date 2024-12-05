package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseException {

    private static final String UNAUTHORIZED_REVIEW = "리뷰 작성자만 %s할 수 있습니다.";

    public AuthorizationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public static AuthorizationException unauthorizedReviewUpdate(String token) {
        return new AuthorizationException(String.format(UNAUTHORIZED_REVIEW, token));
    }

    public static AuthorizationException unauthorizedReviewDelete(String token) {
        return new AuthorizationException(String.format(UNAUTHORIZED_REVIEW, token));
    }

}
