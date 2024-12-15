package com.meong9.backend.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPensionDto {
    private Long pensionId;
    private String pensionName;
    private String address;
    private String placeType;
    @Setter
    private List<String> tags;
    private Double reviewAvg;
    private Integer reviewCount;
    private String weightLimit;
    private Integer lowestPrice;
    private Integer guestCount;
    private Integer petCount;
    private String startTime;
    private String endTime;
    @Setter
    private List<String> images;
    private Boolean likeStatus;
}
