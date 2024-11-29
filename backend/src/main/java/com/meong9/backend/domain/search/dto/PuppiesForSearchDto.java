package com.meong9.backend.domain.search.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PuppiesForSearchDto {
    private final Long memberId;
    private final List<PuppiesWithWeightDto> puppyList;

    public PuppiesForSearchDto(Long memberId, List<PuppiesWithWeightDto> puppyList) {
        this.memberId = memberId;
        this.puppyList = puppyList;
    }
}
