package com.meong9.backend.domain.search.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.search.dto.PuppiesForSearchDto;
import com.meong9.backend.domain.search.dto.SearchPensionsResponseDto;
import com.meong9.backend.domain.search.dto.SearchPlacesResponseDto;
import com.meong9.backend.domain.search.service.SearchService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 시설 검색 컨트롤러
     * regionList: 지역 (ex. 서울) (최대 3개)
     * placeTypes: 장소 카테고리 (ex. 마당) (최대 3개)
     * heaviestDogWeight: 함께 가고자 하는 강아지들 중 가장 무거운 강아지의 무게
     */
    @GetMapping("/places")
    public ResponseEntity<?> searchPlaces(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "regionList", required = false) List<String> regionList,
            @RequestParam(name = "placeTypes", required = false) List<String> placeTypes,
            @RequestParam(name = "heaviestDogWeight", required = false, defaultValue = "0") double heaviestDogWeight,
            @PageableDefault Pageable pageable, // 디폴트 페이지 0, 사이즈 10
            @CurrentMember Member member) {
        Long memberId = (member != null) ? member.getMemberId() : null;
        SearchPlacesResponseDto dto = searchService.searchPlaces(
                searchWord, regionList, placeTypes, heaviestDogWeight, pageable, memberId);
        return CommonResponse.ok("success", dto);
    }


}
