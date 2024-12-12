package com.meong9.backend.domain.meongPhoto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MeongPhotoListDto {

    private final List<MeongPhotoDto> meongPhotoDtoList;
    private boolean hasNext;

}
