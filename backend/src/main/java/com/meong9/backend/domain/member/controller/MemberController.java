package com.meong9.backend.domain.member.controller;

import com.meong9.backend.domain.member.dto.*;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MediaFileService mediaFileService;

    /**
     * 회원 선호시설 추가 컨트롤러
     */
    @PostMapping("/interests/places")
    public ResponseEntity<?> insertPreferredPlaces(@RequestBody InterestDto dto,
                                                   @CurrentMember Member member) {
        memberService.insertPreferredPlaces(dto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 회원 선호지역 추가 컨트롤러
     */
    @PostMapping("/interests/regions")
    public ResponseEntity<?> insertPreferredRegions(@RequestBody RegionDto dto,
                                                    @CurrentMember Member member) {
        memberService.insertPreferredRegions(dto, member);
        return CommonResponse.ok("success");
    }

    @PostMapping("/info")
    public ResponseEntity<?> insertMemberInfo(@RequestPart(name = "ProfileImage", required = false) MultipartFile profileImage,
                                              @Valid @RequestPart(name = "MemberInfoDto") MemberInfoDto dto,
                                              @CurrentMember Member member) throws IOException {
        memberService.insertMemberInfo(profileImage, dto, member);
        return CommonResponse.ok("success");
    }

//    /**
//     * 회원 정보 등록 요청 컨트롤러
//     */
//    @PostMapping("/info")
//    public ResponseEntity<?> insertMemberInfo(@RequestParam(required = false) String fileKey,
//                                              @RequestBody MemberInfoDto dto,
//                                              @CurrentMember Member member) throws IOException {
//        memberService.insertMemberInfo(fileKey, dto, member);
//        return CommonResponse.ok("success");
//    }

    /**
     * 프로필 사진 삭제 요청 컨트롤러
     */
    @DeleteMapping("/images")
    public ResponseEntity<?> deleteProfileImage(@CurrentMember Member member) {
        memberService.deleteProfileImage(member);
        return CommonResponse.ok("success");
    }

    /**
     * 닉네임 중복 여부 확인 컨트롤러
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkNicknameAvailability(@RequestParam(name = "nickname") String nickname) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);
        String message = isAvailable ? "사용 가능한 닉네임입니다." : "중복된 닉네임입니다.";
        return CommonResponse.ok(message);
    }

    /**
     * 마이페이지 조회 컨트롤러
     */
    @GetMapping
    public ResponseEntity<?> getMyPage(@CurrentMember Member member) {
        MypageDto dto = memberService.getMyPage(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 마이페이지 상세 조회 컨트롤러
     */
    @GetMapping("/detail")
    public ResponseEntity<?> getMyPageDetail(@CurrentMember Member member) {
        MyPageDetailDto dto = memberService.getMyPageDetail(member);
        return CommonResponse.ok("success", dto);
    }

    @PatchMapping
    public ResponseEntity<?> updateMyPage(@RequestPart(name = "ProfileImage", required = false) MultipartFile profileImage,
                                          @Valid @RequestPart(name = "UpdateMyPageRequestDto") MemberInfoDto requestDto,
                                          @CurrentMember Member member) throws IOException {
        UpdateMyPageResponseDto dto = memberService.updateMyPage(profileImage, requestDto, member);
        return CommonResponse.ok("success", dto);
    }

//    /**
//     * 마이페이지 수정 컨트롤러
//     */
//    @PatchMapping
//    public ResponseEntity<?> updateMyPage(@RequestParam(required = false) String fileKey,
//                                          @Valid @RequestBody MemberInfoDto requestDto,
//                                          @CurrentMember Member member) throws IOException {
//        UpdateMyPageResponseDto dto = memberService.updateMyPage(fileKey, requestDto, member);
//        return CommonResponse.ok("success", dto);
//    }

    /**
     * 선호 지역 조회 컨트롤러
     */
    @GetMapping("/interests/regions")
    public ResponseEntity<?> getPreferredRegions(@CurrentMember Member member){
        RegionDto dto = memberService.getPreferredRegions(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 선호 지역 수정 컨트롤러
     */
    @PatchMapping("/interests/regions")
    public ResponseEntity<?> updatePreferredRegions(@RequestBody RegionDto regionDto,
                                                    @CurrentMember Member member){
        memberService.insertPreferredRegions(regionDto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 선호 시설 조회 컨트롤러
     */
    @GetMapping("/interests/places")
    public ResponseEntity<?> getPreferredPlaces(@CurrentMember Member member){
        InterestDto dto = memberService.getPreferredPlaces(member);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 선호 시설 수정 컨트롤러
     */
    @PatchMapping("/interests/places")
    public ResponseEntity<?> updatePreferredPlaces(@RequestBody InterestDto interestDto,
                                                   @CurrentMember Member member){
        memberService.insertPreferredPlaces(interestDto, member);
        return CommonResponse.ok("success");
    }

    /**
     * 프로필 이미지 생성을 위한 PreSigned URL 생성
     */
//    @GetMapping("/presigned-url")
//    public ResponseEntity<?> generatePreSignedUrlForMProfileImage(@CurrentMember Member member) {
//        // S3 경로 생성: {folder}/{memberId}_profile.jpg
//        String folder = "Mprofile";
//        String objectKey = String.format("%s/%d_profile.jpg", folder, member.getMemberId());
//
//        Map<String, String> response = mediaFileService.getPresingedUrl(objectKey);
//
//        return CommonResponse.ok("success", response);
//    }

}
