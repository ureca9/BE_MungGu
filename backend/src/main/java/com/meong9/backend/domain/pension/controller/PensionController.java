package com.meong9.backend.domain.pension.controller;

import com.meong9.backend.domain.pension.dto.PensionDetailResponseDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.repository.RoomAvailabilityRepository;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
import com.meong9.backend.domain.pension.dto.RoomResponseDto;
import com.meong9.backend.domain.pension.service.PensionDetailService;
import com.meong9.backend.domain.pension.service.PensionService;
import com.meong9.backend.domain.pension.service.TopPensionService;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PensionController {

    private final PensionDetailService pensionDetailService;
    private final ReviewService reviewService;
    private final PensionService pensionService;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final TopPensionService topPensionService;

    /**
     * 특정 펜션의 상세 데이터를 조회합니다.
     *
     * @param pensionId 펜션 ID
     * @return 특정 펜션에 대한 상세 데이터
     */
    @GetMapping("/pensions/detail/{pensionId}")
    public ResponseEntity<?> getPensionDetail(
            @PathVariable(name = "pensionId") Long pensionId,
            @CurrentMember Member member) {

        Long memberId = (member != null) ? member.getMemberId() : null;

        PensionDetailResponseDto pensionDetail = pensionDetailService.getPensionDetail(pensionId, memberId);

        if(pensionDetail != null) {
            // View count 증가 로직 서비스 호출
            log.info("{}: {}", pensionId, topPensionService.incrementPensionViewCount(pensionId));
        }

        return CommonResponse.ok("success", pensionDetail);
    }

    /**
     * 특정 펜션에서 예약 가능한 방을 조회합니다.
     *
     * @param pensionId 펜션 ID
     * @param startDate 예약 시작 날짜
     * @param endDate 예약 종료 날짜
     * @return 예약 가능한 방 목록
     */
    @GetMapping("/{pensionId}/rooms") // GET 요청을 처리하는 엔드포인트
    public ResponseEntity<?> getAvailableRooms(
            @PathVariable Long pensionId, // URL 경로에서 펜션 ID를 추출
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, // 시작 날짜를 ISO 형식으로 요청받음
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate   // 종료 날짜를 ISO 형식으로 요청받음
    ) {
        // 서비스에서 모든 작업을 처리하도록 위임
        List<RoomResponseDto> rooms = roomAvailabilityRepository.findAvailableRoomsWithImages(pensionId, startDate, endDate);

        return CommonResponse.ok("success", rooms);
    }

    /**
     * 특정 펜션에 대한 리뷰를 페이징 처리하여 조회하는 API 엔드포인트.
     *
     * @param pensionId 클라이언트가 요청하는 펜션의 ID
     * @param page 클라이언트가 요청하는 페이지 번호 (기본값: 0)
     * @return 페이징 처리된 특정 펜션의 리뷰 데이터와 다음 페이지 여부를 포함한 응답
     */
    @GetMapping("/pensions/{pensionId}/reviews")
    public ResponseEntity<?> getPensionReviews(
            @PathVariable Long pensionId,
            @RequestParam(defaultValue = "0") int page // 기본 페이지 번호는 0으로 설정
    ) {
        // 페이지 요청 객체 생성 (현재 페이지와 페이지 크기)
        Pageable pageable = PageRequest.of(page, 10); // 페이지 크기를 10으로 고정

        // 리뷰 서비스에서 페이징된 리뷰 데이터 조회
        Slice<ReviewSummaryResponseDto> reviews = reviewService.getReviews(
                "020",
                pensionId,
                pageable
        );

        // 응답 데이터 생성 (리뷰 목록과 다음 페이지 존재 여부 포함)
        return ResponseEntity.ok(Map.of(
                "reviews", reviews.getContent(), // 현재 페이지의 리뷰 목록
                "hasNext", reviews.hasNext() // 다음 페이지가 존재하는지 여부
        ));
    }

    /**
     * 특정 펜션에 대한 요약 정보를 조회하는 API 엔드포인트.
     *
     * @param pensionId 클라이언트가 요청하는 펜션의 ID
     * @return 특정 펜션에 대한 요약 정보를 포함한 응답
     */
    @GetMapping("/pensions/{pensionId}/summary")
    public ResponseEntity<?> getPensionSummary(
            @PathVariable Long pensionId
    ){
        PensionSummaryResponseDto pensionSummaryResponseDto = pensionService.getPensionSummaryResponseDto(pensionId);// 리뷰와 관련된 펜션 정보 요약
        return CommonResponse.ok("success", pensionSummaryResponseDto);
    }

    /**
     * 인기 펜션 Top 9를 조회하는 엔드포인트
     * 최근 7일간의 조회수를 기준으로 상위 9개 펜션을 반환
     *
     * @return 상위 9개 펜션 정보 응답
     */
    @GetMapping("/pensions/top")
    public ResponseEntity<?> getTopPlacesByCategory(){
        return CommonResponse.ok("success", topPensionService.getTop9PensionsByCategory());
    }
}
