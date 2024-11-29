package com.meong9.backend.domain.search.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.search.dto.PuppiesForSearchDto;
import com.meong9.backend.domain.search.service.SearchService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 강아지 프로필 목록 조회 컨트롤러
     */
    @GetMapping("/puppies")
    public ResponseEntity<?> getPuppiesForSearch(@CurrentMember Member member) {
        PuppiesForSearchDto dto = searchService.getPuppiesForSearch(member);
        return CommonResponse.ok("success", dto);
    }

}
