package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.place.dto.PlaceInfoDto;
import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public PlaceSummaryResponseDto getPlaceSummaryById(Long id) {
        return placeRepository.findPlaceSummaryResponseDtoById(id)
                .orElseThrow(() -> NotFoundException.entityNotFound("시설"));
    }

    /**
     * 주어진 장소 ID에 해당하는 Place 엔티티를 조회합니다.
     * 해당 ID로 Place 엔티티를 찾을 수 없는 경우 NotFoundException을 발생시킵니다.
     * @param placeId 장소 ID
     * @return 제공된 장소 ID에 해당하는 PlaceInfoDto
     * @throws NotFoundException 제공된 ID로 Place 엔티티를 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public PlaceInfoDto getPlaceInfoDto(Long placeId, Long memberId) {
        return placeRepository.findPlaceInfoById(placeId, memberId)
                .orElseThrow(()->NotFoundException.entityNotFound("시설"));
    }
}
