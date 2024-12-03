package com.meong9.backend.domain.review.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewMainDto {
    private Long id; // 펜션 or 시설 아이디
    private String name; // 펜션 or 시설 이름
    private String address;
    private String img; // 대표 이미지 한장
    private String reviewAvg; // 별점
    private Integer reviewCount; // 리뷰 개수
    private String reviewContent; // 리뷰 내용
    private String nickname; // 작성자 닉네임

    private Long reviewId; // 리뷰 아이디
    private String type; // 펜션 or 시설 타입
}
