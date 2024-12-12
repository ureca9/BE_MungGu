package com.meong9.backend.domain.pension.dto;

import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class PensionDetailResponseDto {
    private final Long pensionId;
    private final String pensionName;
    private final Integer reviewCount;
    private final Double reviewAvg;
    private final String address;
    private final List<String> tags;
    private final String startTime;
    private final String endTime;
    private final String telNo;
    private final String latitude;
    private final String longitude;
    private final String description;
    private final String enterPetSize;
    private final String info;
    private final String introduction;
    private final String limitInfo;
    private final Boolean likeStatus;

    private final List<String> images;

    private final List<PhotoReviewSummaryResponseDto> photoReviewList;
    private final List<ReviewSummaryResponseDto> review;

    /**
     * PensionDetailResponseDto를 생성하는 정적 팩토리 메서드.
     *
     * @param pensionInfo 펜션 정보 DTO
     * @param address 주소 문자열
     * @param tags 태그 리스트
     * @param images 이미지 리스트
     * @param photoReviewSummaryList 사진 리뷰 요약 리스트 (Slice 객체)
     * @param reviewSummaryList 일반 리뷰 요약 리스트
     * @return PensionDetailResponseDto 객체
     */
    public static PensionDetailResponseDto of(PensionInfoDto pensionInfo,List<String> tags, List<String> images , String address,
                                              Slice<PhotoReviewSummaryResponseDto> photoReviewSummaryList,
                                              List<ReviewSummaryResponseDto> reviewSummaryList) {
        return PensionDetailResponseDto.builder()
                .pensionId(pensionInfo.getPensionId())
                .pensionName(pensionInfo.getPensionName())
                .reviewCount(pensionInfo.getReviewCount())
                .reviewAvg(pensionInfo.getReviewAvg())
                .address(address)
                .tags(tags)
                .startTime(pensionInfo.getStartTime())
                .endTime(pensionInfo.getEndTime())
                .telNo(pensionInfo.getTelNo())
                .latitude(pensionInfo.getLatitude())
                .longitude(pensionInfo.getLongitude())
                .description(pensionInfo.getDescription())
                .enterPetSize(pensionInfo.getEnterPetSize())
                .info(pensionInfo.getInfo())
                .introduction(pensionInfo.getIntroduction())
                .limitInfo(pensionInfo.getLimitInfo())
                .images(images)
                .likeStatus(pensionInfo.getLikeStatus())
                // photoReviewList 조건 처리
                .photoReviewList(photoReviewSummaryList.getContent().size() >= 4
                        ? photoReviewSummaryList.getContent() // 4개 이상일 경우 내용 추가
                        : Collections.emptyList())
                .review(reviewSummaryList)
                .build();
    }
}
