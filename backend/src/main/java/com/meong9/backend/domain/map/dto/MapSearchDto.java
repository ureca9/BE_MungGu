package com.meong9.backend.domain.map.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class MapSearchDto {
    private final List<MapPlaceDto> content;
    private final boolean hasNext;

    public MapSearchDto(List<MapPlaceDto> content, boolean hasNext) {
        this.content = content;
        this.hasNext = hasNext;
    }
}
