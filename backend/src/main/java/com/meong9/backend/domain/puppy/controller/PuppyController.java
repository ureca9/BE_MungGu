package com.meong9.backend.domain.puppy.controller;

import com.meong9.backend.domain.puppy.dto.PuppyProfileResponseDto;
import com.meong9.backend.domain.puppy.dto.PuppyRequestDto;
import com.meong9.backend.domain.puppy.dto.PuppyResponseDto;
import com.meong9.backend.domain.puppy.service.BreedService;
import com.meong9.backend.domain.puppy.service.PuppyService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/puppies")
@RequiredArgsConstructor
@Slf4j
public class PuppyController {

    private final PuppyService puppyService;
    private final BreedService breedService;

    @PostMapping
    public ResponseEntity<?> createPuppy(
            @RequestPart("data") PuppyRequestDto puppyRequestDto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @CurrentMember Member member) throws IOException {
        // PuppyService를 통해 강아지 프로필 생성
        PuppyResponseDto responseDto = puppyService.createPuppy(puppyRequestDto, image, member);

        // CommonResponse로 응답 반환
        return CommonResponse.created("success", responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getPuppyProfile(@RequestParam Long puppyId) {
        // PuppyService를 통해 강아지 프로필 조회
        PuppyProfileResponseDto puppyProfileResponseDto = puppyService.getPuppyProfile(puppyId);

        // CommonResponse로 응답 반환
        return CommonResponse.ok("success", puppyProfileResponseDto);
    }

    @PatchMapping
    public ResponseEntity<?> updatePuppy(
            @RequestParam Long puppyId,
            @RequestPart("data") PuppyRequestDto updateRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        log.debug("Received update data: {}", updateRequest);

        // PuppyService를 통해 강아지 정보 수정
        PuppyResponseDto updatedPuppy = puppyService.updatePuppy(puppyId, updateRequest, image);

        // CommonResponse로 응답 반환
        return CommonResponse.ok("success", updatedPuppy);
    }

    @DeleteMapping
    public ResponseEntity<?> deletePuppy(@RequestParam Long puppyId) {
        puppyService.deletePuppyById(puppyId);
        return CommonResponse.ok("success");
    }

    @GetMapping("/types")
    public ResponseEntity<?> getBreeds() {
        return CommonResponse.ok("success", breedService.findAllBreeds());
    }
}