package com.meong9.backend.domain.search.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.domain.search.dto.PuppiesForSearchDto;
import com.meong9.backend.domain.search.dto.PuppiesWithWeightDto;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import com.meong9.backend.domain.search.dto.SearchPlacesResponseDto;
import com.meong9.backend.domain.search.repository.SearchJooqRepository;
import com.meong9.backend.global.repository.RegionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.meong9.backend.jooq.generated.Tables.PLACE;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {
    private final PuppyRepository puppyRepository;
    private final RegionRepository regionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlcCategoryRepository plcCategoryRepository;
    private final SearchJooqRepository searchRepository;
    private final DSLContext dsl;

    /**
     * 멤버의 모든 강아지를 puppyId 기준으로 정렬하여 조회하는 서비스 메서드
     */
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
    public SearchPlacesResponseDto searchPlaces(List<String> regionList, List<String> placeTypes,
                                                double heaviestDogWeight, Pageable pageable, Long memberId) {
        // 1. 지역 + 카테고리 조건으로 시설 ID 필터링
        List<Long> regionIds = (regionList == null || regionList.isEmpty())
                ? regionRepository.findAllRegionIds()
                : regionRepository.findRegionIdsByNameIn(regionList);

        String typeCode = "020"; // 시설 코드
        List<Long> placeIdsByRegion = plcPenAddressRepository.findPlaceIdsByRegionIdIn(regionIds, typeCode);

        List<Long> categoryIds = (placeTypes == null || placeTypes.isEmpty())
                ? plcCategoryRepository.findAllCategoryIds()
                : plcCategoryRepository.findPlcCategoryIdsByNameIn(placeTypes);

        // 2. 반려견 체중 조건 추가
        String sizeCode = heaviestDogWeight < 10 ? "010" : heaviestDogWeight < 25 ? "020" : "030";

        // 3. 최종 필터링된 시설 조회
        Slice<SearchPlaceDto> filteredPlaces =
                searchRepository.searchPlaces(
                placeIdsByRegion,
                categoryIds,
                sizeCode,
                typeCode,
                pageable,
                memberId);

        // 4. 반환
        return new SearchPlacesResponseDto(filteredPlaces.getContent(), filteredPlaces.hasNext());
    }

}
