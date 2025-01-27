package com.meong9.backend.domain.review.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.PresignedUrlDto;
import com.meong9.backend.domain.review.dto.ReviewMainDto;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.dto.ReviewUrlRequestDto;
import com.meong9.backend.domain.review.service.ReviewCreationService;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ReviewCreationService reviewCreationService;
    private final MediaFileService mediaFileService;

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

    @PostMapping("/reviews/presigned-url")
    public ResponseEntity<?> getPresignedUrlForReview(@RequestBody ReviewUrlRequestDto reviewUrlRequestDto) {
        List<PresignedUrlDto> presignedUrls = mediaFileService.getPresignedUrlForReview(reviewUrlRequestDto);
        return ResponseEntity.ok(presignedUrls);
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDto requestDto,
                                               @CurrentMember Member member) {
        // @PreAuthorize로 관리할 경우 403을 반환하기 때문에 200을 반환하되 메시지를 명확히 적어 주도록 함
        if (member.getBlackList() != null && member.getBlackList().getLockedUntil().isAfter(LocalDateTime.now())) {
            String date = member.getBlackList().getLockedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String message = String.format("%s 까지 리뷰 작성 권한이 제한됩니다.", date);
            return CommonResponse.ok(message);
        }
        reviewCreationService.createReviewWithVideos(requestDto, member);
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
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @CurrentMember Member member) throws IllegalAccessException {
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


//    @PostMapping("/reviews")
//    public ResponseEntity<?> createReview(
//            @Valid @RequestPart("data") ReviewRequestDto reviewRequestDto,
//            @RequestPart(value = "file", required = false) List<MultipartFile> files,
//            @CurrentMember Member member)  {
//        // @PreAuthorize로 관리할 경우 403을 반환하기 때문에 200을 반환하되 메시지를 명확히 적어 주도록 함
//        if (member.getBlackList() != null && member.getBlackList().getLockedUntil().isAfter(LocalDateTime.now())) {
//            String date = member.getBlackList().getLockedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String message = String.format("%s 까지 리뷰 작성 권한이 제한됩니다.", date);
//            return CommonResponse.ok(message);
//        }
//        reviewService.createReview(reviewRequestDto,files,member);
//        return CommonResponse.created("success");
//    }

}
