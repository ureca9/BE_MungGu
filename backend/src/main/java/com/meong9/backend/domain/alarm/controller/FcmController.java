package com.meong9.backend.domain.alarm.controller;

import com.meong9.backend.domain.alarm.dto.FcmRequestDto;
import com.meong9.backend.domain.alarm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    // FCM 토큰 저장
    @PostMapping("/api/v1/fcm/token")
    public ResponseEntity<String> receiveFcmToken(@RequestBody FcmRequestDto fcmRequestDto) {
        String token = fcmRequestDto.getToken();  // 클라이언트에서 보낸 token 값을 추출
        Long memberId = fcmRequestDto.getMemberId();

        fcmService.saveToken(memberId, token,604800); // 7일

        // 응답 반환
        return ResponseEntity.ok("Token received");
    }
}
