package com.meong9.backend.domain.meongPhoto.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoResponseDto;
import com.meong9.backend.domain.meongPhoto.service.MeongPhotoService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
public class MeongPhotoController {

    private final MeongPhotoService meongPhotoService;

    /**
     * 멍생네컷 저장 컨트롤러
     */
    @PostMapping()
    public ResponseEntity<?> createMeongPhoto(@CurrentMember Member member,
                                              @RequestPart(name = "image") MultipartFile image) throws IOException {
        MeongPhotoResponseDto dto = meongPhotoService.createMeongPhoto(member, image);
        return CommonResponse.ok("success", dto);
    }
}
