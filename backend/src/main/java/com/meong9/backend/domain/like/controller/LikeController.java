package com.meong9.backend.domain.like.controller;

import com.meong9.backend.domain.like.service.LikeService;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.auth.entity.MemberDetails;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/places/likes/{placeId}")
    public ResponseEntity<?> togglePlaceLike(@CurrentMember Member member,@PathVariable Long placeId) {
        return CommonResponse.created(likeService.togglePlaceLike(member, placeId));
    }

    @PostMapping("/pensions/likes/{pensionId}")
    public ResponseEntity<?> togglePensionLike(@CurrentMember Member member, @PathVariable Long pensionId) {
        return CommonResponse.created(likeService.togglePensionLike(member, pensionId));
    }

    // Place 즐겨찾기 상태 조회
    @GetMapping("/places/likes/{placeId}")
    public ResponseEntity<?> getPlaceLikeStatus(@CurrentMember Member member,@PathVariable Long placeId) {
        return CommonResponse.ok(likeService.isPlaceLikedByMember(member, placeId) ? "찜한 시설입니다." : "찜하지 않은 시설입니다.");

    }

    // Pension 즐겨찾기 상태 조회
    @GetMapping("/pensions/likes/{pensionId}")
    public ResponseEntity<?> getPensionLikeStatus(@CurrentMember Member member, @PathVariable Long pensionId) {
        return CommonResponse.ok(likeService.isPensionLikedByMember(member, pensionId) ? "찜한 펜션입니다." : "찜하지 않은 펜션입니다.");
    }
}
