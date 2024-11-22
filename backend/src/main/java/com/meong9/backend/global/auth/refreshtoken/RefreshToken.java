package com.meong9.backend.global.auth.refreshtoken;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class RefreshToken {
    @Id
    private String keyEmail;

    private String refreshToken;

    private long expiration;

    public RefreshToken(String keyEmail, String refreshToken) {
        this.keyEmail = keyEmail;
        this.refreshToken = refreshToken;
        this.expiration = 604800L; // 7일
    }

    public void updateToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        this.expiration = 604800L; // 만료 시간 초기화
    }
}
