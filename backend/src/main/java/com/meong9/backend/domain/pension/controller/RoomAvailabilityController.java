package com.meong9.backend.domain.pension.controller;

import com.meong9.backend.domain.pension.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomAvailabilityController {
    private final RoomAvailabilityService roomAvailabilityService;

    @PostMapping("/availability/{days}")
    public ResponseEntity<?> addAvailability(@PathVariable int days) {
        roomAvailabilityService.addAvailabilityForDays(days);
        return ResponseEntity.ok("30일치 Room Availability 데이터가 생성되었습니다.");
    }
}