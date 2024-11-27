package com.meong9.backend.domain.member.controller;

import com.meong9.backend.domain.member.dto.*;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.service.KakaoService;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.auth.utils.JwtProvider;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> refreshAccessToken(@CookieValue(name = "Refresh-token") String refreshToken) {
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
                                                   @CurrentMember Member member) {
        memberService.insertPreferredPlaces(dto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 회원 선호지역 추가 컨트롤러
     */
    @PostMapping("/members/interests/regions")
    public ResponseEntity<?> insertPreferredRegions(@RequestBody RegionDto dto,
                                                    @CurrentMember Member member) {
        memberService.insertPreferredRegions(dto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 회원 정보 등록 요청 컨트롤러
     */
    @PostMapping("/members/info")
    public ResponseEntity<?> insertMemberInfo(@RequestPart(name = "ProfileImage", required = false) MultipartFile profileImage,
                                              @Valid @RequestPart(name = "MemberInfoDto") MemberInfoDto dto,
                                              @CurrentMember Member member) throws IOException {
        memberService.insertMemberInfo(profileImage, dto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 프로필 사진 삭제 요청 컨트롤러
     */
    @DeleteMapping("/members/images")
    public ResponseEntity<?> deleteProfileImage(@CurrentMember Member member) {
        memberService.deleteProfileImage(member);
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

    /**
     * 마이페이지 조회 컨트롤러
     */
    @GetMapping("/members")
    public ResponseEntity<?> getMyPage(@CurrentMember Member member) {
        MypageDto dto = memberService.getMyPage(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 마이페이지 상세 조회 컨트롤러
     */
    @GetMapping("/members/detail")
    public ResponseEntity<?> getMyPageDetail(@CurrentMember Member member) {
        MyPageDetailDto dto = memberService.getMyPageDetail(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 마이페이지 수정 컨트롤러
     */
    @PatchMapping("/members")
    public ResponseEntity<?> updateMyPage(@RequestPart(name = "ProfileImage", required = false) MultipartFile profileImage,
                                          @Valid @RequestPart(name = "UpdateMyPageRequestDto") MemberInfoDto requestDto,
                                          @CurrentMember Member member) throws IOException {
        UpdateMyPageResponseDto dto = memberService.updateMyPage(profileImage, requestDto, member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 선호 지역 조회 컨트롤러
     */
    @GetMapping("/members/interests/regions")
    public ResponseEntity<?> getPreferredRegions(@CurrentMember Member member){
        RegionDto dto = memberService.getPreferredRegions(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 선호 지역 수정 컨트롤러
     */
    @PatchMapping("/members/interests/regions")
    public ResponseEntity<?> updatePreferredRegions(@RequestBody RegionDto regionDto,
                                                    @CurrentMember Member member){
        memberService.insertPreferredRegions(regionDto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 선호 시설 조회 컨트롤러
     */
    @GetMapping("/members/interests/places")
    public ResponseEntity<?> getPreferredPlaces(@CurrentMember Member member){
        InterestDto dto = memberService.getPreferredPlaces(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 선호 시설 수정 컨트롤러
     */
    @PatchMapping("/members/interests/places")
    public ResponseEntity<?> updatePreferredPlaces(@RequestBody InterestDto interestDto,
                                                   @CurrentMember Member member){
        memberService.insertPreferredPlaces(interestDto, member);
        return CommonResponse.ok("success");
    }
}
