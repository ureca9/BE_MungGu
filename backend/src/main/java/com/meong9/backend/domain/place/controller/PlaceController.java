package com.meong9.backend.domain.place.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.place.dto.PlaceDetailResponseDto;
import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.service.PlaceDetailService;
import com.meong9.backend.domain.place.service.PlaceService;
import com.meong9.backend.domain.place.service.TopPlaceService;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.utils.CategoryMapper;
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
    private final TopPlaceService topPlaceService;

    @GetMapping("/places/detail/{placeId}")
    public ResponseEntity<?> getPlaceDetail(
            @PathVariable(name = "placeId") Long placeId,
            @CurrentMember Member member) {

        Long memberId = (member != null) ? member.getMemberId() : null;

        PlaceDetailResponseDto placeDetail = placeDetailService.getPlaceDetail(placeId, memberId);

        // View count 증가 로직 서비스 호출
        log.info("{}: {}", placeDetail.getCategory() + placeId, topPlaceService.incrementCategoryViewCount(placeDetail.getCategory(), placeId));

        return CommonResponse.ok("success", placeDetail);
    }

    @GetMapping("/places/{placeId}/reviews")
    public ResponseEntity<?> getPlaceReviews(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page // 클라이언트가 요청하는 페이지 번호
    ) {
        Pageable pageable = PageRequest.of(page, 5); // 페이지 크기를 5으로 고정
        Slice<ReviewSummaryResponseDto> reviews = reviewService.getReviews(
                "010",
                placeId,
                pageable
        );

        return ResponseEntity.ok(Map.of(
                "reviews", reviews.getContent(),
                "hasNext", reviews.hasNext()
        ));
    }

    /**
     * 특정 시설의 전체 리뷰 조회 페이지를 위한 시설 요약 조회
     */
    @GetMapping("/places/{placeId}/summary")
    public ResponseEntity<?> getPlaceSummary(
            @PathVariable long placeId
    ){
        PlaceSummaryResponseDto placeSummary = placeService.getPlaceSummaryById(placeId);
        return CommonResponse.ok("success", placeSummary);
    }

    /**
     * 카테고리별 인기 장소 Top 9를 조회하는 엔드포인트
     * 최근 7일간의 조회수를 기준으로 상위 9개 장소를 반환
     *
     * @param category 장소 카테고리
     * @return 상위 9개 장소 정보 응답
     */
    @GetMapping("/places/{category}/top")
    public ResponseEntity<?> getTopPlacesByCategory(
            @PathVariable(value = "category") String category) {
        if(CategoryMapper.isValidCategoryId(category)){
            return CommonResponse.ok("success", topPlaceService.getTop9PlacesByCategory(category));
        }else {
            log.warn("Invalid category requested: {}", category);
            throw new BadRequestException("Invalid category: " + category);
        }
    }
}
