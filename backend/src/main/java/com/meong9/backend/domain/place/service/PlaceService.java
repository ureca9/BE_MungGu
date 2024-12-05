package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;


    public PlaceSummaryResponseDto getPlaceSummaryById(Long id) {
        return placeRepository.findPlaceSummaryResponseDtoById(id)
                .orElseThrow(() -> NotFoundException.entityNotFound("시설"));
    }
}
