package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUrlRequestDto {
    private Long plcPenId;
    private String type;
    private List<String> files;
}
