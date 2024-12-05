package com.meong9.backend.domain.place.controller;

import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.service.PlaceDetailService;
import com.meong9.backend.domain.place.service.PlaceService;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PlaceController {
    private final PlaceDetailService placeDetailService;
    private final ReviewService reviewService;
    private final PlaceService placeService;

    @GetMapping("/places/detail/{placeId}")
    public ResponseEntity<?> getPlaceDetail(@PathVariable(name = "placeId") Long placeId) {
        return CommonResponse.ok("success", placeDetailService.getPlaceDetail(placeId));
    }

    @GetMapping("/places/{placeId}/reviews")
    public ResponseEntity<?> getPlaceReviews(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page // 클라이언트가 요청하는 페이지 번호
    ) {
        Pageable pageable = PageRequest.of(page, 10); // 페이지 크기를 10으로 고정
        Slice<ReviewSummaryResponseDto> reviews = reviewService.getReviews("010", placeId, pageable);

        return ResponseEntity.ok(Map.of(
                "reviews", reviews.getContent(),
                "hasNext", reviews.hasNext()
        ));
    }

    @GetMapping("/places/{placeId}/summary")
    public ResponseEntity<?> getPlaceSummary(
            @PathVariable long placeId
    ){
        PlaceSummaryResponseDto placeSummary = placeService.getPlaceSummaryById(placeId);
        return CommonResponse.ok("success", placeSummary);
    }
}
