package com.meong9.backend.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchPlacesResponseDto {
    private final List<SearchPlaceDto> placeInfo;
    private final boolean hasNext;
}
