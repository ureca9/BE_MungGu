package com.meong9.backend.domain.meongPhoto.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoListDto;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoResponseDto;
import com.meong9.backend.domain.meongPhoto.service.MeongPhotoService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping
    public ResponseEntity<?> getAllMeongPhoto(@RequestParam(name = "lastPhotoId", required = false) Long lastPhotoId,
                                              @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        MeongPhotoListDto dto = meongPhotoService.getAllMeongPhoto(lastPhotoId, size);
        return CommonResponse.ok("success", dto);
    }
}
