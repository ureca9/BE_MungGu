package com.meong9.backend.domain.map.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.map.dto.MapPlaceDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.search.repository.SearchJooqRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import com.meong9.backend.global.utils.DistanceMapper;
import com.meong9.backend.global.utils.TypeCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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

    // 장소 조회
    @Transactional(readOnly = true)
    public MapPlaceDto getSelectPlcPen(Member member, Long id, String type, Double userLatitude, Double userLongitude) {
        if ("펜션".equals(type)) {
            return getPensionDetails(member, id, userLatitude, userLongitude);
        } else if ("시설".equals(type)) {
            return getPlaceDetails(member, id, userLatitude, userLongitude);
        }
        return null;
    }

    // 장소 검색
    @Transactional(readOnly = true)
    public Page<MapPlaceDto> getSearchPlcPen(Member member, String searchWord, Double userLatitude, Double userLongitude, Pageable pageable) {
        // 검색어로 조회
        List<Long> placeIds = searchJooqRepository.findPlaceIdsBySearchWord(searchWord);
        List<Long> pensionIds = searchJooqRepository.findPensionIdsBySearchWord(searchWord);

        // Place와 Pension ID로 조회
        List<Object[]> places = placeRepository.findAllWithLikeStatus(placeIds, member);  // List로 Place 조회
        List<Object[]> pensions = pensionRepository.findAllWithLikeStatus(pensionIds, member);  // List로 Pension 조회

        // PlcPenAddress에서 주소 가져오기
        List<PlcPenAddress> pensionAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIds, "020");
        List<PlcPenAddress> placeAddresses = plcPenAddressRepository.findAddressesByIdsAndType(placeIds, "010");

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

            String mainImage = !place.getPlaceFiles().isEmpty() ? place.getPlaceFiles().get(0).getMediaFile().getFileUrl() : null;
            String address = placeAddressMap.getOrDefault(place.getPlaceId(), null);
            String businessHour = place.getBusinessHour();

            MapPlaceDto mapPlaceDto = createMapPlaceDto(
                    place.getPlaceId(),
                    TypeCodeMapper.getType("010"),
                    place.getName(),
                    latitude,
                    longitude,
                    mainImage,
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

            String mainImage = !pension.getPensionFiles().isEmpty() ? pension.getPensionFiles().get(0).getMediaFile().getFileUrl() : null;
            String address = pensionAddressMap.getOrDefault(pension.getPensionId(), null);

            MapPlaceDto mapPlaceDto = createMapPlaceDto(
                    pension.getPensionId(),
                    TypeCodeMapper.getType("020"),
                    pension.getName(),
                    latitude,
                    longitude,
                    mainImage,
                    distance,
                    address,
                    null,
                    isLike
            );
            allResults.add(mapPlaceDto);
        }

        // 거리 정렬
        List<MapPlaceDto> sortedResults = sortByDistance(allResults);

        // 페이지 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedResults.size());

        // 페이지 처리된 결과 생성
        List<MapPlaceDto> pageContent = sortedResults.subList(start, end);

        // PageImpl 생성하여 반환
        return new PageImpl<>(pageContent, pageable, sortedResults.size());
    }


    private MapPlaceDto getPensionDetails(Member member, Long id, Double userLatitude, Double userLongitude) {
        Pension pension = pensionRepository.findByPensionIdWithImage(id)
                .orElseThrow(() -> new NotFoundException("펜션을 찾을 수 없습니다."));

        List<PlcPenAddress> addresses = plcPenAddressRepository.findAddressesByIdsAndType(Collections.singletonList(id), "020");
        PlcPenAddress addressEntity = addresses.isEmpty() ? null : addresses.get(0);

        String latitude = pension.getLatitude();
        String longitude = pension.getLongitude();
        String mainImage = !pension.getPensionFiles().isEmpty() ? pension.getPensionFiles().get(0).getMediaFile().getFileUrl() : null;
        Double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
        String address = addressEntity != null ? addressEntity.getAddress().getAddress() : null;
        boolean isLike = likeRepository.existsByMemberAndPensionId(member, id);

        return createMapPlaceDto(pension.getPensionId(), TypeCodeMapper.getType("020"), pension.getName(),
                latitude, longitude, mainImage, distance, address, null, isLike);
    }

    private MapPlaceDto getPlaceDetails(Member member, Long id, Double userLatitude, Double userLongitude) {
        Place place = placeRepository.findByPlaceIdWithImage(id)
                .orElseThrow(() -> new NotFoundException("시설을 찾을 수 없습니다."));

        List<PlcPenAddress> addresses = plcPenAddressRepository.findAddressesByIdsAndType(Collections.singletonList(id), "010");
        PlcPenAddress addressEntity = addresses.isEmpty() ? null : addresses.get(0);

        String latitude = place.getLatitude();
        String longitude = place.getLongitude();
        String mainImage = !place.getPlaceFiles().isEmpty() ? place.getPlaceFiles().get(0).getMediaFile().getFileUrl() : null;
        Double distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
        String address = addressEntity != null ? addressEntity.getAddress().getAddress() : null;
        String businessHour = place.getBusinessHour();
        boolean isLike = likeRepository.existsByMemberAndPlaceId(member, id);

        return createMapPlaceDto(place.getPlaceId(), TypeCodeMapper.getType("010"), place.getName(),
                latitude, longitude, mainImage, distance, address, businessHour, isLike);
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
