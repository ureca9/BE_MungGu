package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ReviewInfoQueryResult {
    private Place place;
    private Pension pension;
    private Long reviewCount;

    public ReviewInfoQueryResult(Place place, Long reviewCount) {
        this.place = place;
        this.reviewCount = reviewCount;
    }

    public ReviewInfoQueryResult(Pension pension, Long reviewCount) {
        this.pension = pension;
        this.reviewCount = reviewCount;
    }
}
