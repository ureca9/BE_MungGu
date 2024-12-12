package com.meong9.backend.domain.map.controller;

import com.meong9.backend.domain.map.dto.*;
import com.meong9.backend.domain.map.service.MapSearchService;
import com.meong9.backend.domain.map.service.MapService;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;
    private final MapSearchService mapSearchService;

    // 카테고리 별 찜 상세 조회
    @GetMapping("/likes/detail")
    public ResponseEntity<?> getMapLikeDetails(@CurrentMember Member member, @RequestParam(name = "categoryName") String categoryName,
                                               @RequestParam(name = "latitude") Double latitude, @RequestParam(name = "longitude") Double longitude,
                                               Pageable pageable){
        int maxPageSize = 1000; // 최대 페이지 사이즈 설정
        if (pageable.getPageSize() > maxPageSize) {
            pageable = PageRequest.of(pageable.getPageNumber(), maxPageSize, pageable.getSort());
        }

        MapLikeResponseDto likeDetails = mapService.getMapLikeDetails(member, categoryName, latitude, longitude, pageable);

        return CommonResponse.ok("success", likeDetails);
    }

    // 장소 검색
    @GetMapping("/search")
    public ResponseEntity<?> getSearchPlcPen(@CurrentMember Member member, @RequestParam(name = "keyword") String keyword,
                                             @RequestParam(name = "latitude") Double latitude, @RequestParam(name = "longitude") Double longitude,
                                             Pageable pageable){

        int maxPageSize = 1000; // 최대 페이지 사이즈 설정
        if (pageable.getPageSize() > maxPageSize) {
            pageable = PageRequest.of(pageable.getPageNumber(), maxPageSize, pageable.getSort());
        }

        MapSearchDto dto = mapSearchService.getSearchPlcPen(member, keyword, latitude, longitude, pageable);

        return CommonResponse.ok("success", dto);
    }

    // 찜한 장소 위도, 경도 조회 (마커용)
    @GetMapping("/likes/points")
    public ResponseEntity<?> getMapLikePoints(@CurrentMember Member member){
        List<MapLikePointDto> mapPointList = mapService.getMapLikePoints(member);

        return CommonResponse.ok("success", mapPointList);
    }

    // 장소 조회
    @GetMapping("/places")
    public ResponseEntity<?> getSelectPlcPen(@CurrentMember Member member,
                                             @RequestParam("id") Long id, @RequestParam("type") String type,
                                             @RequestParam(name = "latitude") Double latitude, @RequestParam(name = "longitude") Double longitude){
        MapPlaceSelectDto mapPlaceDto = mapSearchService.getSelectPlcPen(member, id, type, latitude, longitude);

        return CommonResponse.ok("success", mapPlaceDto);
    }
}
