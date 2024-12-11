package com.meong9.backend.domain.search.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.domain.search.dto.*;
import com.meong9.backend.domain.search.repository.SearchJooqRepository;
import com.meong9.backend.global.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final PuppyRepository puppyRepository;
    private final RegionRepository regionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlcCategoryRepository plcCategoryRepository;
    private final SearchJooqRepository searchRepository;

    /**
     * 멤버의 모든 강아지를 puppyId 기준으로 정렬하여 조회하는 서비스 메서드
     */
    @Transactional(readOnly = true)
    public PuppiesForSearchDto getPuppiesForSearch(Member member) {
        List<Puppy> puppies = puppyRepository.findByMemberIdWithPuppyProfileImage(member.getMemberId());
        List<PuppiesWithWeightDto> puppiesWithWeightDto = puppies.stream()
                .map(puppy -> PuppiesWithWeightDto.builder()
                        .puppyId(puppy.getPuppyId())
                        .puppyWeight(puppy.getWeight())
                        .puppyName(puppy.getName())
                        .puppyImageUrl(puppy.getProfileImage().getFileUrl())
                        .build())
                .toList();
        return new PuppiesForSearchDto(member.getMemberId(), puppiesWithWeightDto);
    }

    /**
     * 1) 지역, 2) 장소 카테고리, 3) 강아지 무게 를 기반으로 필터링하여 장소를 검색하는 서비스 메서드
     */
    @Transactional(readOnly = true)
    public SearchPlacesResponseDto searchPlaces(String searchWord, List<String> regionList, List<String> placeTypes,
                                                double heaviestDogWeight, Pageable pageable, Long memberId) {
        String typeCode = "010"; // 시설 코드

        List<Long> firstFilteredPlaceIds; // 1차로 필터링된 시설 ID 목록

        if (searchWord != null && !searchWord.trim().isEmpty()) {
            // 1. 검색어로 시설 ID 필터링
            firstFilteredPlaceIds = searchRepository.findPlaceIdsBySearchWord(searchWord);
        } else {
            // 2. 지역으로 시설 ID 필터링
            List<Long> regionIds = (regionList == null || regionList.isEmpty())
                    ? regionRepository.findAllRegionIds()
                    : regionRepository.findRegionIdsByNameIn(regionList);

            firstFilteredPlaceIds = plcPenAddressRepository.findFacilityIdsByRegionIdIn(regionIds, typeCode);
        }

        List<Long> categoryIds = (placeTypes == null || placeTypes.isEmpty())
                ? plcCategoryRepository.findAllCategoryIds()
                : plcCategoryRepository.findPlcCategoryIdsByNameIn(placeTypes);

        // 2. 반려견 체중 조건 추가
        String sizeCode = heaviestDogWeight < 10 ? "010" : heaviestDogWeight < 25 ? "020" : "030";

        // 3. 최종 필터링된 placeId 조회
        Slice<Long> secondFilteredPlaceIds = searchRepository.findPlaceIdsMatchWithCategoryIds(firstFilteredPlaceIds, categoryIds, sizeCode, pageable);

        // 4. placeId를 가지고 정보 조회
        List<SearchPlaceDto> filteredPlaces =
                searchRepository.searchPlaces(
                secondFilteredPlaceIds.getContent(),
                categoryIds,
                typeCode,
                memberId);

        // 4. 반환
        return new SearchPlacesResponseDto(filteredPlaces, secondFilteredPlaceIds.hasNext());
    }

    @Transactional(readOnly = true)
    public SearchPensionsResponseDto searchPensions(String searchWord, List<String> regionList,
                                                    double heaviestDogWeight, String startDate, String endDate,
                                                    Pageable pageable, Long memberId) {
        String typeCode = "020"; // 펜션 코드

        Slice<Long> filteredPensionIds; // 최종 필터링된 펜션 ID 목록

        if (searchWord != null && !searchWord.trim().isEmpty()) {
            // 1. 검색어로 펜션 ID 필터링
            filteredPensionIds = searchRepository.findPensionIdsBySearchWord(searchWord, pageable);
        } else {
            // 2. 지역으로 펜션 ID 필터링
            List<Long> regionIds = (regionList == null || regionList.isEmpty())
                    ? regionRepository.findAllRegionIds()
                    : regionRepository.findRegionIdsByNameIn(regionList);

            filteredPensionIds = plcPenAddressRepository.findFacilityIdsByRegionIdInWithPagination(regionIds, typeCode, pageable);
        }

        // 2. 반려견 체중 조건 추가
        String sizeCode = heaviestDogWeight < 10 ? "010" : heaviestDogWeight < 25 ? "020" : "030";

        // 3. 최종 필터링된 시설 조회
        List<SearchPensionDto> filteredPensions =
                searchRepository.searchPensions(
                        filteredPensionIds.getContent(),
                        startDate,
                        endDate,
                        sizeCode,
                        typeCode,
                        memberId);

        // 4. 반환
        return new SearchPensionsResponseDto(filteredPensions, filteredPensionIds.hasNext());
    }
}
