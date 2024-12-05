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
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.DistanceMapper;
import com.meong9.backend.global.utils.TypeCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.meong9.backend.domain.map.dto.MapPlaceDto.createMapPlaceDto;

@Service
@RequiredArgsConstructor
public class MapSearchService {

    private final PensionRepository pensionRepository;
    private final PlaceRepository placeRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final LikeRepository likeRepository;

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
}
