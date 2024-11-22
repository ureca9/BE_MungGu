package com.meong9.backend.domain.member.controller;

import com.meong9.backend.domain.member.dto.LoginResponseDto;
import com.meong9.backend.domain.member.service.KakaoService;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.global.auth.utils.JwtProvider;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberController {
    private final KakaoService kakaoService;
    private final MemberService memberService;

    /**
     * 카카오 로그인 처리 컨트롤러
     */
    @PostMapping("/callback/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam(name = "code") String code, HttpServletResponse response) throws IOException {
        LoginResponseDto dto = kakaoService.kakaoLogin(code, response);
        return CommonResponse.ok("success", dto);
    }

    /**
     * access token 재발급 요청 처리 컨트롤러
     */
    @PostMapping("/token")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
        String newAccessToken = memberService.refreshAccessToken(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);
        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("message", "Access Token이 재발급되었습니다."));
    }
}
