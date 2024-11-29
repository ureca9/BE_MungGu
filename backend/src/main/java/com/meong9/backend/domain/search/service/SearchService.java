package com.meong9.backend.domain.search.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceFileRepository;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.place.repository.PlaceTagRepository;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.domain.search.dto.PuppiesForSearchDto;
import com.meong9.backend.domain.search.dto.PuppiesWithWeightDto;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import com.meong9.backend.domain.search.dto.SearchPlacesResponseDto;
import com.meong9.backend.global.entity.Tag;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.repository.RegionRepository;
import com.meong9.backend.global.repository.TagRepository;
import com.meong9.backend.global.utils.EnterPetSizeMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {
    private final PuppyRepository puppyRepository;
    private final RegionRepository regionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlcCategoryRepository plcCategoryRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagRepository placeTagRepository;
    private final PlaceFileRepository placeFileRepository;
    private final LikeRepository likeRepository;

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
        // 1. 지역 조건과 시설 유형 조건으로 시설 ID 조회
        List<Long> regionIds = (regionList == null || regionList.isEmpty())
                ? regionRepository.findAllRegionIds()
                : regionRepository.findRegionIdsByNameIn(regionList);

        String typeCode = "020"; // 시설 코드
        List<Long> placeIdsByRegion = plcPenAddressRepository.findPlaceIdsByRegionIdIn(regionIds, typeCode);

        // 2. 카테고리 ID 조회
        List<Long> categoryIds = (placeTypes == null || placeTypes.isEmpty())
                ? plcCategoryRepository.findAllCategoryIds()
                : plcCategoryRepository.findPlcCategoryIdsByNameIn(placeTypes);

        // 3. 반려견 체중 조건 추가
        String sizeCode = heaviestDogWeight < 10 ? "010" : heaviestDogWeight < 25 ? "020" : "030";

        // 4. 최종 필터링된 시설 조회
        Slice<SearchPlaceDto> filteredPlaces = placeRepository.findPlacesByCondition(placeIdsByRegion, categoryIds, sizeCode, pageable);

        // 5. 결과 DTO로 매핑
        List<Long> placeIds = filteredPlaces.getContent().stream()
                .map(SearchPlaceDto::getPlaceId)
                .collect(Collectors.toList());

        // Address 조회 및 Map 생성
        Map<Long, String> addressMap = plcPenAddressRepository.findAddressesByPlaceIdsAndType(placeIds, typeCode).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // placeId
                        result -> (String) result[1] // address
                ));

        // Tags 조회 및 Map 생성
        Map<Long, List<String>> tagMap = placeTagRepository.findPlaceTagsByPlaceIds(placeIds).stream()
                .collect(Collectors.groupingBy(
                        result -> (Long) result[0], // placeId
                        Collectors.mapping(result -> (String) result[1], Collectors.toList()) // tags
                ));

        // Images 조회 및 Map 생성
        Map<Long, List<String>> imageMap = placeFileRepository.findImagesByPlaceIds(placeIds).stream()
                .collect(Collectors.groupingBy(
                        result -> (Long) result[0], // placeId
                        Collectors.mapping(result -> (String) result[1], Collectors.toList()) // images
                ));

        // LikeStatus 조회 및 Map 생성
        Map<Long, Boolean> likedPlaces = memberId == null || placeIds.isEmpty()
                ? Collections.emptyMap()
                : likeRepository.findLikeStatusPlaceIds(memberId, placeIds).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // placeId
                        result -> (Boolean) result[1] // likeStatus
                ));

        // 6. 병합 및 DTO 생성
        List<SearchPlaceDto> completedDtos = filteredPlaces.getContent().stream()
                .map(dto -> {
                    dto.setWeightLimit(EnterPetSizeMapper.getEnterPetSize(dto.getWeightLimit())); // EnterPetSize 변환 후 재매핑
                    dto.setAddress(addressMap.getOrDefault(dto.getPlaceId(), null)); // Address 매핑
                    dto.setTags(tagMap.getOrDefault(dto.getPlaceId(), Collections.emptyList())); // Tags 매핑
                    dto.setImages(imageMap.getOrDefault(dto.getPlaceId(), Collections.emptyList())); // Images 매핑
                    dto.setLikeStatus(likedPlaces.getOrDefault(dto.getPlaceId(), false)); // LikeStatus 매핑
                    return dto;
                })
                .collect(Collectors.toList());

        // 7. 응답 생성
        return new SearchPlacesResponseDto(completedDtos, filteredPlaces.hasNext());
    }
}
