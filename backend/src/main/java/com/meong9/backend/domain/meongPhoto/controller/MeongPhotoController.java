package com.meong9.backend.domain.meongPhoto.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoListDto;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoResponseDto;
import com.meong9.backend.domain.meongPhoto.dto.MyMeongPhotoListDto;
import com.meong9.backend.domain.meongPhoto.service.MeongPhotoService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
public class MeongPhotoController {

    private final MeongPhotoService meongPhotoService;

    /**
     * 멍생네컷 저장 컨트롤러
     */
    @PostMapping
    public ResponseEntity<?> createMeongPhoto(@CurrentMember Member member,
                                              @RequestPart(name = "image") MultipartFile image,
                                              HttpServletRequest request) {
        String serviceUrl = request.getRequestURI();
        MeongPhotoResponseDto dto = meongPhotoService.createMeongPhoto(member, image, serviceUrl);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 멍생네컷 전체 조회 컨트롤러
     */
    @GetMapping
    public ResponseEntity<?> getAllMeongPhoto(@PageableDefault(page = 0, size = 6) Pageable pageable) {
        MeongPhotoListDto dto = meongPhotoService.getAllMeongPhoto(pageable);
        return CommonResponse.ok("success", dto);
    }

    /**
     * 멤버의 본인 멍생네컷 조회 컨트롤러
     */
    @GetMapping("/mine")
    public ResponseEntity<?> getMyMeongPhotos(@CurrentMember Member member,
                                              @PageableDefault(page = 0, size = 6) Pageable pageable) {
        MyMeongPhotoListDto dto = meongPhotoService.getMyMeongPhotos(member, pageable);
        return CommonResponse.ok("success", dto);
    }
}
