package com.meong9.backend.global.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException {

    private static final String UNAUTHENTICATED_TOKEN = "토큰 '%s'는 유효하지 않습니다.";
    private static final String SOCIAL_LOGIN_ERROR = "카카오 인증에 실패하였습니다.";
    private static final String NO_USER_ROLE = "사용자 역할 정보가 없습니다.";
    private static final String NO_REFRESH_TOKEN = "리프레시 토큰이 없습니다.";
    private static final String FETCH_USERDATA_ERROR = "카카오에서 사용자 정보를 가져오던 중 문제가 발생하였습니다.";

    public AuthenticationException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public static AuthenticationException unauthenticatedToken(String token) {
        return new AuthenticationException(String.format(UNAUTHENTICATED_TOKEN, token));
    }

    public static AuthenticationException socialLoginError() {
        return new AuthenticationException(SOCIAL_LOGIN_ERROR);
    }

    public static AuthenticationException noUserRole() {
        return new AuthenticationException(NO_USER_ROLE);
    }

    public static AuthenticationException noRefreshToken() {
        return new AuthenticationException(NO_REFRESH_TOKEN);
    }

    public static AuthenticationException fetchUserdataError() {
        return new AuthenticationException(FETCH_USERDATA_ERROR);
    }
}
