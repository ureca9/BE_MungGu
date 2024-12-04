package com.meong9.backend.domain.place.controller;

import com.meong9.backend.domain.place.service.PlaceDetailService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PlaceController {
    private final PlaceDetailService placeDetailService;

    @GetMapping("/places/detail/{placeId}")
    public ResponseEntity<?> getPlaceDetail(@PathVariable(name = "placeId") Long placeId) {
        return CommonResponse.ok("success", placeDetailService.getPlaceDetail(placeId));
    }
}
