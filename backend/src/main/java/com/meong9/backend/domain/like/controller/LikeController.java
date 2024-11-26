package com.meong9.backend.domain.like.controller;

import com.meong9.backend.domain.like.service.LikeService;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.global.auth.entity.MemberDetails;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class LikeController {
    private final LikeService likeService;
    private final MemberService memberService;

    @PostMapping("/places/likes/{placeId}")
    public ResponseEntity<?> togglePlaceLike(@AuthenticationPrincipal MemberDetails memberDetails,@PathVariable Long placeId) {
        Member member=memberDetails.member();
        log.debug("회원 이메일 = {}", member.getEmail());
        String message = likeService.togglePlaceLike(member, placeId);
        return CommonResponse.created("success");
    }

    @PostMapping("/pensions/likes/{pensionId}")
    public ResponseEntity<?> togglePensionLike(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable Long pensionId) {
        Member member=memberDetails.member();
        String message = likeService.togglePensionLike(member, pensionId);
        return CommonResponse.created("success");
    }

}
