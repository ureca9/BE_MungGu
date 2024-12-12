package com.meong9.backend.domain.map.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.map.dto.MapPlaceDto;
import com.meong9.backend.domain.map.dto.MapPlaceSelectDto;
import com.meong9.backend.domain.map.dto.MapSearchDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.repository.PlaceFileRepository;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.search.repository.SearchJooqRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import com.meong9.backend.global.utils.DistanceMapper;
import com.meong9.backend.global.utils.ImageMapper;
import com.meong9.backend.global.utils.TypeCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.meong9.backend.domain.map.dto.MapPlaceDto.createMapPlaceDto;

@Service
@RequiredArgsConstructor
public class MapSearchService {

    private final PensionRepository pensionRepository;
    private final PlaceRepository placeRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final LikeRepository likeRepository;
    private final SearchJooqRepository searchJooqRepository;
    private final PlaceFileRepository placeFileRepository;

    // 장소 조회
    @Transactional(readOnly = true)
    public MapPlaceSelectDto getSelectPlcPen(Member member, Long id, String type, Double userLatitude, Double userLongitude) {
        if ("펜션".equals(type)) {
            return getPensionDetails(member, id, userLatitude, userLongitude);
        } else if ("시설".equals(type)) {
            return getPlaceDetails(member, id, userLatitude, userLongitude);
        }
        return null;
    }

    // 장소 검색
    @Transactional(readOnly = true)
    public MapSearchDto getSearchPlcPen(Member member, String searchWord, Double userLatitude, Double userLongitude, Pageable pageable) {
        // 검색어로 조회
        Slice<Long> placeIds = searchJooqRepository.findPlaceIdsBySearchWordForMap(searchWord, pageable);
        Slice<Long> pensionIds = searchJooqRepository.findPensionIdsBySearchWordForMap(searchWord, pageable);


        // Place와 Pension ID로 조회
        List<Object[]> places = placeRepository.findAllWithLikeStatus(placeIds.getContent(), member);  // List로 Place 조회
        List<Object[]> pensions = pensionRepository.findAllWithLikeStatus(pensionIds.getContent(), member);  // List로 Pension 조회

        // PlcPenAddress에서 주소 가져오기
        List<PlcPenAddress> pensionAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIds.getContent(), "020");
        List<PlcPenAddress> placeAddresses = plcPenAddressRepository.findAddressesByIdsAndType(placeIds.getContent(), "010");

        // 주소 매핑
        Map<Long, String> pensionAddressMap = AddressMapper.mapAddressesByPlcPenId(pensionAddresses);
        Map<Long, String> placeAddressMap = AddressMapper.mapAddressesByPlcPenId(placeAddresses);

        // DTO
        List<MapPlaceDto> allResults = new ArrayList<>();

        // Place DTO 변환
        for (Object[] placeResult : places) {
            Place place = (Place) placeResult[0];
            boolean isLike = (boolean) placeResult[1];

            String latitude = place.getLatitude();
            String longitude = place.getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }

            List<String> images = !place.getPlaceFiles().isEmpty() ? ImageMapper.getPlaceImageUrl(place) : null;
            String address = placeAddressMap.getOrDefault(place.getPlaceId(), null);
            String businessHour = place.getBusinessHour();

            MapPlaceDto mapPlaceDto = createMapPlaceDto(
                    place.getPlaceId(),
                    TypeCodeMapper.getType("010"),
                    place.getName(),
                    latitude,
                    longitude,
                    images,
                    distance,
                    address,
                    businessHour,
                    isLike
            );
            allResults.add(mapPlaceDto);
        }

        // Pension DTO 변환
        for (Object[] pensionResult : pensions) {
            Pension pension = (Pension) pensionResult[0];
            boolean isLike = (boolean) pensionResult[1];

            String latitude = pension.getLatitude();
            String longitude = pension.getLongitude();

            Double distance = null;
            if (latitude != null && longitude != null) {
                distance = DistanceMapper.calculateDistance(userLatitude, userLongitude,
                        Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
            }

            List<String> images = !pension.getPensionFiles().isEmpty() ? ImageMapper.getPensionImageUrl(pension) : null;
            String address = pensionAddressMap.getOrDefault(pension.getPensionId(), null);

            MapPlaceDto mapPlaceDto = createMapPlaceDto(
                    pension.getPensionId(),
                    TypeCodeMapper.getType("020"),
                    pension.getName(),
                    latitude,
                    longitude,
                    images,
                    distance,
                    address,
                    null,
                    isLike
            );
            allResults.add(mapPlaceDto);
        }

        boolean hasNext = placeIds.hasNext() || pensionIds.hasNext();

        // 거리 정렬 후 반환
        return new MapSearchDto(sortByDistance(allResults), hasNext);
    }


    private MapPlaceSelectDto getPensionDetails(Member member, Long id, Double userLatitude, Double userLongitude) {
        Pension pension = pensionRepository.findByPensionIdWithImage(id)
                .orElseThrow(() -> new NotFoundException("펜션을 찾을 수 없습니다."));

        List<PlcPenAddress> addresses = plcPenAddressRepository.findAddressesByIdsAndType(Collections.singletonList(id), "020");
        PlcPenAddress addressEntity = addresses.isEmpty() ? null : addresses.get(0);

        String latitude = pension.getLatitude();
        String longitude = pension.getLongitude();
        List<String> images = !pension.getPensionFiles().isEmpty() ? ImageMapper.getPensionImageUrlAll(pension) : null;
        Double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
        String address = addressEntity != null ? addressEntity.getAddress().getAddress() : null;
        boolean isLike = likeRepository.existsByMemberAndPensionId(member, id);

        return MapPlaceSelectDto.createMapPlaceDto(pension.getPensionId(), TypeCodeMapper.getType("020"), pension.getName(),
                latitude, longitude, images, distance, address, null, isLike);
    }

    private MapPlaceSelectDto getPlaceDetails(Member member, Long id, Double userLatitude, Double userLongitude) {
        Place place = placeRepository.findByPlaceIdWithImage(id)
                .orElseThrow(() -> new NotFoundException("시설을 찾을 수 없습니다."));

        List<PlcPenAddress> addresses = plcPenAddressRepository.findAddressesByIdsAndType(Collections.singletonList(id), "010");
        PlcPenAddress addressEntity = addresses.isEmpty() ? null : addresses.get(0);

        String latitude = place.getLatitude();
        String longitude = place.getLongitude();
        List<String> images = !place.getPlaceFiles().isEmpty() ? ImageMapper.getPlaceImageUrlAll(place) : null;
        Double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
        String address = addressEntity != null ? addressEntity.getAddress().getAddress() : null;
        String businessHour = place.getBusinessHour();
        boolean isLike = likeRepository.existsByMemberAndPlaceId(member, id);

        return MapPlaceSelectDto.createMapPlaceDto(place.getPlaceId(), TypeCodeMapper.getType("010"), place.getName(),
                latitude, longitude, images, distance, address, businessHour, isLike);
    }

    private Double calculateDistance(Double userLatitude, Double userLongitude, String latitude, String longitude) {
        if (latitude != null && longitude != null) {
            return DistanceMapper.calculateDistance(userLatitude, userLongitude,
                    Double.parseDouble(latitude), Double.parseDouble(longitude));
        }
        return null;
    }

    // 거리 순 정렬
    private List<MapPlaceDto> sortByDistance(List<MapPlaceDto> mapPlaceDtos){
        mapPlaceDtos.sort(
                Comparator.comparing(
                        MapPlaceDto::getDistance, // Double 값을 반환
                        Comparator.nullsLast(Double::compareTo) // null은 마지막으로 처리
                )
        );
        return mapPlaceDtos;
    }
}
