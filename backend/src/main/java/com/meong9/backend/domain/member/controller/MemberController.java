package com.meong9.backend.domain.member.controller;

import com.meong9.backend.domain.member.dto.InterestDto;
import com.meong9.backend.domain.member.dto.LoginResponseDto;
import com.meong9.backend.domain.member.dto.MemberInfoDto;
import com.meong9.backend.domain.member.dto.RegionDto;
import com.meong9.backend.domain.member.service.KakaoService;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.global.auth.entity.MemberDetails;
import com.meong9.backend.global.auth.utils.JwtProvider;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {
    private final KakaoService kakaoService;
    private final MemberService memberService;

    /**
     * 카카오 로그인 처리 컨트롤러
     */
    @PostMapping("/auth/callback/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam(name = "code") String code, HttpServletResponse response) throws IOException {
        LoginResponseDto dto = kakaoService.kakaoLogin(code, response);
        return CommonResponse.ok("success", dto);
    }

    /**
     * access token 재발급 요청 처리 컨트롤러
     */
    @PostMapping("/auth/token")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
        String newAccessToken = memberService.refreshAccessToken(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);
        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("message", "Access Token이 재발급되었습니다."));
    }

    /**
     * 회원 선호시설 추가 컨트롤러
     */
    @PostMapping("/members/interests/places")
    public ResponseEntity<?> insertPreferredPlaces(@RequestBody InterestDto dto,
                                                   @AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.insertPreferredPlaces(dto, memberDetails.member());
        return CommonResponse.ok("success");
    }

    /**
     * 회원 선호지역 추가 컨트롤러
     */
    @PostMapping("/members/interests/regions")
    public ResponseEntity<?> insertPreferredRegions(@RequestBody RegionDto dto,
                                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.insertPreferredRegions(dto, memberDetails.member());
        return CommonResponse.ok("success");
    }

    /**
     * 회원 정보 등록 요청 컨트롤러
     */
    @PostMapping("/members/info")
    public ResponseEntity<?> insertMemberInfo(@RequestPart(name = "ProfileImage", required = false) MultipartFile profileImage,
                                              @RequestPart(name = "MemberInfoDto") MemberInfoDto dto,
                                              @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        memberService.insertMemberInfo(profileImage, dto, memberDetails.member());
        return CommonResponse.ok("success");
    }

    /**
     * 프로필 사진 삭제 요청 컨트롤러
     */
    @DeleteMapping("/members/images")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.deleteProfileImage(memberDetails.member());
        return CommonResponse.ok("success");
    }

    /**
     * 닉네임 중복 여부 확인 컨트롤러
     */
    @GetMapping("/members/check")
    public ResponseEntity<?> checkNicknameAvailability(@RequestParam(name = "nickname") String nickname) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);
        String message = isAvailable ? "사용 가능한 닉네임입니다." : "중복된 닉네임입니다.";
        return CommonResponse.ok(message);
    }
}
