package com.meong9.backend.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    @NotNull(message = "plcPenId는 필수 입력값입니다.")
    private Long plcPenId; // 시설 또는 펜션 ID

    @Setter
    @NotBlank(message = "내용을 입력하세요.")
    private String content; // 리뷰 내용

    @NotBlank(message = "펜션인지 시설인지 명시해 주세요")
    private String type; // 펜션/시설 구분

    @NotNull(message = "별점은 필수 입력값입니다.")
    @Min(value = 1, message = "별점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점이어야 합니다.")
    private Float score; // 별점

    @NotNull(message = "방문일은 필수 입력값입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visitDate; // 방문 날짜

}
