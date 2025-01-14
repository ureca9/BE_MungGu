package com.meong9.backend.domain.alarm.controller;

import com.meong9.backend.domain.alarm.dto.FcmRequestDto;
import com.meong9.backend.domain.alarm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/api/fcm")
    public ResponseEntity pushMessage(@RequestBody FcmRequestDto requestDTO) throws IOException {
        System.out.println(requestDTO.getTargetToken() + " "
                +requestDTO.getTitle() + " " + requestDTO.getBody());

        fcmService.sendMessageTo(
                requestDTO.getTargetToken(),
                requestDTO.getTitle(),
                requestDTO.getBody());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/fcm/token")
    public ResponseEntity<String> receiveFcmToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");  // 클라이언트에서 보낸 token 값을 추출

        // FCM 토큰을 데이터베이스에 저장하거나 처리하는 로직 추가
        System.out.println("Received token: " + token);

        // 응답 반환
        return ResponseEntity.ok("Token received");
    }
}
