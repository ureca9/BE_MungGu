package com.meong9.backend.domain.pension.controller;

import com.meong9.backend.domain.pension.dto.RoomResponseDto;
import com.meong9.backend.domain.pension.service.PensionDetailService;
import com.meong9.backend.domain.pension.service.RoomService;
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
    private final RoomService roomService; // Room 데이터를 처리하는 서비스 클래스
    private final ReviewService reviewService;

    @GetMapping("/pensions/detail/{pensionId}")
    public ResponseEntity<?> getPensionDetail(@PathVariable(name = "pensionId") Long pensionId) {
        return CommonResponse.ok("success", pensionDetailService.getPensionDetail(pensionId));
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
        List<RoomResponseDto> rooms = roomService.findAvailableRoomsWithImages(pensionId, startDate, endDate);

        return CommonResponse.ok("success", rooms);
    }

    /**
     * 특정 펜션에 대한 리뷰를 페이징 처리하여 조회하는 API 엔드포인트.
     *
     * @param pensionId 클라이언트가 요청하는 펜션의 ID
     * @param page 클라이언트가 요청하는 페이지 번호 (기본값: 0)
     * @return 페이징 처리된 리뷰 데이터와 다음 페이지 여부를 포함한 응답
     */
    @GetMapping("/pensions/{pensionId}/reviews")
    public ResponseEntity<?> getPensionReviews(
            @PathVariable Long pensionId,
            @RequestParam(defaultValue = "0") int page // 기본 페이지 번호는 0으로 설정
    ) {
        // 페이지 요청 객체 생성 (현재 페이지와 페이지 크기)
        Pageable pageable = PageRequest.of(page, 10); // 페이지 크기를 10으로 고정

        // 리뷰 서비스에서 페이징된 리뷰 데이터 조회
        Slice<ReviewSummaryResponseDto> reviews = reviewService.getReviews("020", pensionId, pageable);

        // 응답 데이터 생성 (리뷰 목록과 다음 페이지 존재 여부 포함)
        return ResponseEntity.ok(Map.of(
                "reviews", reviews.getContent(), // 현재 페이지의 리뷰 목록
                "hasNext", reviews.hasNext() // 다음 페이지가 존재하는지 여부
        ));
    }

}
