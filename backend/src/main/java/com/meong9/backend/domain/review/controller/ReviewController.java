package com.meong9.backend.domain.review.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.ReviewMainDto;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(
            @Valid @RequestPart("data") ReviewRequestDto reviewRequestDto,
            @RequestPart(value = "image", required = false) List<MultipartFile> files,
            @CurrentMember Member member) throws IOException {
        reviewService.createReview(reviewRequestDto,files,member);
        return CommonResponse.ok("success");
    }

    @GetMapping("/spots/reviews")
    public ResponseEntity<?> getRecentReview(){
        List<ReviewMainDto> reviewList = reviewService.getRecentReviews();

        Map<String, List<ReviewMainDto>> response = new HashMap<>();
        response.put("review", reviewList);

        return CommonResponse.ok("success", response);
    }

}
