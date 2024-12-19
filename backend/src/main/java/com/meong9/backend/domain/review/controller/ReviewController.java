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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<?> getReviewDetails(@PathVariable Long reviewId) {
        return CommonResponse.ok("success",reviewService.getReviewDetails(reviewId));
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getMyReviews(@CurrentMember Member member) {
        return CommonResponse.ok("success",reviewService.getMyReviews(member));
    }

    @GetMapping("/reviews/info")
    public ResponseEntity<?> getPlacePensionInfo(@RequestParam(required = true) String type,@RequestParam(required = true) Long id) {
        return CommonResponse.ok("success", reviewService.getPlacePensionInfo(type,id));
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(
            @Valid @RequestPart("data") ReviewRequestDto reviewRequestDto,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @CurrentMember Member member)  {
        // @PreAuthorize로 관리할 경우 403을 반환하기 때문에 200을 반환하되 메시지를 명확히 적어 주도록 함
        if (member.getBlackList() != null && member.getBlackList().getLockedUntil().isAfter(LocalDateTime.now())) {
            return CommonResponse.ok("리뷰 작성 권한이 없습니다.");
        }
        reviewService.createReview(reviewRequestDto,files,member);
        return CommonResponse.created("success");
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("data") ReviewRequestDto reviewRequestDto,
            @RequestPart(value = "file", required = false) List<MultipartFile> newFiles,
            @CurrentMember Member member) throws IOException, IllegalAccessException, InterruptedException, TimeoutException {
        reviewService.updateReview(reviewId, reviewRequestDto, newFiles, member);
        return CommonResponse.ok("success");
    }


    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @CurrentMember Member member) throws IOException, IllegalAccessException {
        reviewService.deleteReview(reviewId, member);
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
