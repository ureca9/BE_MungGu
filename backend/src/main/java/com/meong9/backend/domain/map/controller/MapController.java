package com.meong9.backend.domain.map.controller;

import com.meong9.backend.domain.map.dto.MapLikePointDto;
import com.meong9.backend.domain.map.service.MapService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    // 카테고리 별 찜 상세 조회
//    @GetMapping("/likes/detail")
//    public ResponseEntity<?> getMapLikeDetails(){
//
//    }

    // 장소 검색
//    @GetMapping("/search")
//    public ResponseEntity<?> getSearchPlcPen(@RequestParam(name = "keyword") String keyword){
//
//    }

    // 찜한 장소 위도, 경도 조회 (마커용)
    @GetMapping("/likes/points")
    public ResponseEntity<?> getMapLikePoints(){
        List<MapLikePointDto> mapPointList = mapService.getMapLikePoints();

        return CommonResponse.ok("success", mapPointList);
    }

    // 장소 조회
//    @GetMapping("/places")
//    public ResponseEntity<?> getSelectPlcPen(){
//
//    }
}
