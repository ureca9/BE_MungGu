package com.meong9.backend.global.auth.refreshtoken;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void insertRefreshToken(String keyEmail, String refreshToken) {
        Optional<RefreshToken> findToken = refreshTokenRepository.findById(keyEmail);
        if (findToken.isPresent()){
            findToken.get().updateToken(refreshToken);
            refreshTokenRepository.save(findToken.get());
        } else {
            refreshTokenRepository.save(new RefreshToken(keyEmail, refreshToken));
        }
    }

    public boolean validateRefreshToken(String keyEmail, String token) {
        return refreshTokenRepository.findById(keyEmail)
                .map(storedToken -> storedToken.getRefreshToken().equals(token))
                .orElse(false);
    }

    public Optional<RefreshToken> getRefreshToken(String keyEmail) {
        return refreshTokenRepository.findById(keyEmail);
    }

    public void removeRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
}
