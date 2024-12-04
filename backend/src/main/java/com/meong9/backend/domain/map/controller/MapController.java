package com.meong9.backend.domain.map.controller;

import com.meong9.backend.domain.map.dto.MapLikePointDto;
import com.meong9.backend.domain.map.dto.MapLikeResponseDto;
import com.meong9.backend.domain.map.service.MapService;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 카테고리 별 찜 상세 조회
    @GetMapping("/likes/detail")
    public ResponseEntity<?> getMapLikeDetails(@CurrentMember Member member, @RequestParam(name = "categoryName") String categoryName, @RequestParam(name = "latitude") Double latitude, @RequestParam(name = "longitude") Double longitude, Pageable pageable){
        MapLikeResponseDto likeDetails = mapService.getMapLikeDetails(member, categoryName, latitude, longitude, pageable);

        return CommonResponse.ok("success", likeDetails);
    }

    // 장소 검색
    @GetMapping("/search")
    public ResponseEntity<?> getSearchPlcPen(@RequestParam(name = "keyword") String keyword){
        return CommonResponse.ok("success", null);
    }

    // 찜한 장소 위도, 경도 조회 (마커용)
    @GetMapping("/likes/points")
    public ResponseEntity<?> getMapLikePoints(@CurrentMember Member member){
        List<MapLikePointDto> mapPointList = mapService.getMapLikePoints(member);

        return CommonResponse.ok("success", mapPointList);
    }
    // 장소 조회
    @GetMapping("/places")
    public ResponseEntity<?> getSelectPlcPen(){
        return CommonResponse.ok("success", null);
    }
}
