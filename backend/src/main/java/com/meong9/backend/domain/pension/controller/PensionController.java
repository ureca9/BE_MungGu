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

    @GetMapping("/pensions/{pensionId}/reviews")
    public ResponseEntity<?> getPensionReviews(
            @PathVariable Long pensionId,
            @RequestParam(defaultValue = "0") int page // 클라이언트가 요청하는 페이지 번호
    ) {
        Pageable pageable = PageRequest.of(page, 10); // 페이지 크기를 20으로 고정
        Slice<ReviewSummaryResponseDto> reviews = reviewService.getReviews("020", pensionId, pageable);

        return ResponseEntity.ok(Map.of(
                "reviews", reviews.getContent(),
                "hasNext", reviews.hasNext()
        ));
    }
}
