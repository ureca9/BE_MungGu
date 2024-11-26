package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException{
    private static final String EMAIL_ALREADY_IN_USE = "해당 이메일 '%s'은(는) 이미 다른 계정으로 가입되어 있습니다.";

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public static ConflictException emailAlreadyInUse(String email) {
        return new ConflictException(String.format(EMAIL_ALREADY_IN_USE, email));
    }
}
