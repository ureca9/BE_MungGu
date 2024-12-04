package com.meong9.backend.domain.map.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.map.dto.MapLikePlaceDto;
import com.meong9.backend.domain.map.dto.MapLikePointDto;
import com.meong9.backend.domain.map.dto.MapLikeResponseDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.entity.PlcCategory;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import com.meong9.backend.global.utils.DistanceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.meong9.backend.domain.map.dto.MapLikePlaceDto.getPensionToLikePlaceDto;
import static com.meong9.backend.domain.map.dto.MapLikePlaceDto.getPlaceToLikePlaceDto;

@Service
@RequiredArgsConstructor
public class MapService {

    private final LikeRepository likeRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlcCategoryRepository plcCategoryRepository;

    // 찜한 목록 조회 (마커용)
    @Transactional(readOnly = true)
    public List<MapLikePointDto> getMapLikePoints(Member member) {
        List<PensionLike> pensionLikes = getPensionLikes(member);
        List<PlaceLike> placeLikes = getPlaceLikes(member);

        // dto 매핑
        List<MapLikePointDto> mapLikePointList = new ArrayList<>();
        for(PensionLike pensionLike : pensionLikes){
            MapLikePointDto mapLikePointDto = MapLikePointDto.builder()
                    .id(pensionLike.getPension().getPensionId())
                    .type("펜션")
                    .name(pensionLike.getPension().getName())
                    .latitude(pensionLike.getPension().getLatitude())
                    .longitude(pensionLike.getPension().getLongitude())
                    .build();

            mapLikePointList.add(mapLikePointDto);
        }

        for(PlaceLike placeLike : placeLikes){
            MapLikePointDto mapLikePointDto = MapLikePointDto.builder()
                    .id(placeLike.getPlace().getPlaceId())
                    .type("시설")
                    .name(placeLike.getPlace().getName())
                    .latitude(placeLike.getPlace().getLatitude())
                    .longitude(placeLike.getPlace().getLongitude())
                    .build();

            mapLikePointList.add(mapLikePointDto);
        }

        return mapLikePointList;
    }

    // 찜 목록 상세 (카테고리 별 검색 - 전체, 카페, 펜션, 마당, 공원, 놀이터, 섬, 해수욕장) 기본 거리순
    @Transactional(readOnly = true)
    public MapLikeResponseDto getMapLikeDetails(Member member, String categoryName, Double latitude, Double longitude, Pageable pageable) {
        // "전체" 카테고리
        if (categoryName.equals("전체")) {
            Page<PensionLike> pensionLikes = likeRepository.findAllPensionLikesPage(member, pageable);
            Page<PlaceLike> placeLikes = likeRepository.findAllPlaceLikesPage(member, pageable);

            return getAllMapLikes(pensionLikes, placeLikes, latitude, longitude);

        } else if(categoryName.equals("펜션")) { // "펜션" 카테고리
            List<PensionLike> pensionLikes = getPensionLikes(member);

            return getPensionMapLikes(pensionLikes, latitude, longitude);
        } else { // 나머지 시설 카테고리들
            PlcCategory plcCategory = plcCategoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리"));

            List<PlaceLike> placeLikes = likeRepository.findPlaceLikesByCategory(member, plcCategory);

            return getPlaceMapLikes(placeLikes, latitude, longitude, plcCategory);
        }
    }

    // 찜한 장소 거리 기준으로 정렬 -> 시설(카페, 마당, 공원, 놀이터, 섬, 해수욕장)
    private MapLikeResponseDto getPlaceMapLikes(List<PlaceLike> placeLikes, Double userLatitude, Double userLongitude, PlcCategory plcCategory) {
        List<MapLikePlaceDto> mapLikeList = new ArrayList<>();

        List<Long> placeIds = placeLikes.stream()
                .map(p -> p.getPlace().getPlaceId())
                .collect(Collectors.toList());

        // PlcPenAddress에서 주소 가져오기
        List<PlcPenAddress> placeAddresses = plcPenAddressRepository.findAddressesByIdsAndType(placeIds, "010");

        // 주소 매핑
        Map<Long, String> placeAddressMap = AddressMapper.mapAddressesByPlcPenId(placeAddresses);

        // place 정보 dto 변환
        for (PlaceLike placeLike : placeLikes) {
            String latitude = placeLike.getPlace().getLatitude();
            String longitude = placeLike.getPlace().getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }
            String address = placeAddressMap.getOrDefault(placeLike.getPlace().getPlaceId(), null);
            List<String> image = getPlaceImageUrl(placeLike);

            MapLikePlaceDto mapLikePlaceDto = getPlaceToLikePlaceDto(placeLike, latitude, longitude, address, distance, image);

            mapLikeList.add(mapLikePlaceDto);
        }

        // 거리 순 정렬
        List<MapLikePlaceDto> mapLikePlaceDtos = sortByDistance(mapLikeList);

        // MapLikeResponseDto 생성
        MapLikeResponseDto mapLikeResponseDto = MapLikeResponseDto.builder()
                .categoryId(plcCategory.getPlcCategoryId())
                .categoryName(plcCategory.getName())
                .places(mapLikePlaceDtos)
                .build();

        return mapLikeResponseDto;
    }

    // 찜한 장소 거리 기준으로 정렬 -> 펜션
    private MapLikeResponseDto getPensionMapLikes(List<PensionLike> pensionLikes, Double userLatitude, Double userLongitude) {
        List<MapLikePlaceDto> mapLikeList = new ArrayList<>();

        // pensionIds와 placeIds 추출
        List<Long> pensionIds = pensionLikes.stream()
                .map(p -> p.getPension().getPensionId())
                .collect(Collectors.toList());

        // PlcPenAddress에서 주소 가져오기
        List<PlcPenAddress> pensionAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIds, "020");
        Map<Long, String> pensionAddressMap = AddressMapper.mapAddressesByPlcPenId(pensionAddresses);

        // pension 정보 dto 변환
        for (PensionLike pensionLike : pensionLikes) {
            String latitude = pensionLike.getPension().getLatitude();
            String longitude = pensionLike.getPension().getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }
            String address = pensionAddressMap.getOrDefault(pensionLike.getPension().getPensionId(), null);
            List<String> image = getPlaceImageUrl(pensionLike);

            MapLikePlaceDto mapLikePlaceDto = getPensionToLikePlaceDto(pensionLike, latitude, longitude, address, distance, image);
            mapLikeList.add(mapLikePlaceDto);
        }

        // 거리 순 정렬
        List<MapLikePlaceDto> mapLikePlaceDtos = sortByDistance(mapLikeList);

        // MapLikeResponseDto 생성
        MapLikeResponseDto mapLikeResponseDto = MapLikeResponseDto.builder()
                .categoryId(0L)
                .categoryName("펜션")
                .places(mapLikePlaceDtos)
                .build();

        return mapLikeResponseDto;
    }

    // 찜한 장소 거리 기준으로 정렬 -> 전체
    private MapLikeResponseDto getAllMapLikes(
            Page<PensionLike> pensionLikes,
            Page<PlaceLike> placeLikes,
            Double userLatitude,
            Double userLongitude) {

        List<MapLikePlaceDto> mapLikeList = new ArrayList<>();

        // pensionIds와 placeIds 추출
        List<Long> pensionIds = pensionLikes.stream()
                .map(p -> p.getPension().getPensionId())
                .collect(Collectors.toList());

        List<Long> placeIds = placeLikes.stream()
                .map(p -> p.getPlace().getPlaceId())
                .collect(Collectors.toList());

        // PlcPenAddress에서 각각의 주소 가져오기
        List<PlcPenAddress> pensionAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIds, "020");
        List<PlcPenAddress> placeAddresses = plcPenAddressRepository.findAddressesByIdsAndType(placeIds, "010");

        // 주소 매핑
        Map<Long, String> pensionAddressMap = AddressMapper.mapAddressesByPlcPenId(pensionAddresses);
        Map<Long, String> placeAddressMap = AddressMapper.mapAddressesByPlcPenId(placeAddresses);


        // pension 정보 dto 변환
        for (PensionLike pensionLike : pensionLikes) {
            String latitude = pensionLike.getPension().getLatitude();
            String longitude = pensionLike.getPension().getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }
            String address = pensionAddressMap.getOrDefault(pensionLike.getPension().getPensionId(), null);
            List<String> image = getPlaceImageUrl(pensionLike);

            MapLikePlaceDto mapLikePlaceDto = getPensionToLikePlaceDto(pensionLike, latitude, longitude, address, distance, image);
            mapLikeList.add(mapLikePlaceDto);
        }

        // place 정보 dto 변환
        for (PlaceLike placeLike : placeLikes) {
            String latitude = placeLike.getPlace().getLatitude();
            String longitude = placeLike.getPlace().getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }
            String address = placeAddressMap.getOrDefault(placeLike.getPlace().getPlaceId(), null);
            List<String> image = getPlaceImageUrl(placeLike);

            MapLikePlaceDto mapLikePlaceDto = getPlaceToLikePlaceDto(placeLike, latitude, longitude, address, distance, image);
            mapLikeList.add(mapLikePlaceDto);
        }

        // 거리 순 정렬
        List<MapLikePlaceDto> mapLikePlaceDtos = sortByDistance(mapLikeList);

        // MapLikeResponseDto 생성
        MapLikeResponseDto mapLikeResponseDto = MapLikeResponseDto.builder()
                .categoryId(null)
                .categoryName("전체")
                .places(mapLikePlaceDtos)
                .build();

        return mapLikeResponseDto;
    }

    // PensionLike 이미지 URL 추출
    private List<String> getPlaceImageUrl(PensionLike pensionLike) {
        List<String> imageUrls = new ArrayList<>();
        if (!pensionLike.getPension().getPensionFiles().isEmpty()) {
            for (PensionFile pensionFile : pensionLike.getPension().getPensionFiles()) {
                if (imageUrls.size() < 3) {
                    imageUrls.add(pensionFile.getMediaFile().getFileUrl());
                } else {
                    break;
                }
            }
        }
        return imageUrls;
    }

    // PlaceLike 이미지 URL 추출
    private List<String> getPlaceImageUrl(PlaceLike placeLike) {
        List<String> imageUrls = new ArrayList<>();
        if (!placeLike.getPlace().getPlaceFiles().isEmpty()) {
            for (PlaceFile placeFile : placeLike.getPlace().getPlaceFiles()) {
                if (imageUrls.size() < 3) {
                    imageUrls.add(placeFile.getMediaFile().getFileUrl());
                } else {
                    break;
                }
            }
        }
        return imageUrls;
    }

    // 사용자가 찜한 펜션
    private List<PensionLike> getPensionLikes(Member member){
        return likeRepository.findAllPensionLikes(member);
    }

    // 사용자가 찜한 시설
    private List<PlaceLike> getPlaceLikes(Member member){
        return likeRepository.findAllPlaceLikes(member);
    }

    // 거리 순 정렬
    private List<MapLikePlaceDto> sortByDistance(List<MapLikePlaceDto> mapLikeList){
        mapLikeList.sort(
                Comparator.comparing(
                        MapLikePlaceDto::getDistance, // Double 값을 반환
                        Comparator.nullsLast(Double::compareTo) // null은 마지막으로 처리
                )
        );
        return mapLikeList;
    }

}
