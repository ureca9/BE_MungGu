package com.meong9.backend.domain.place.dto;

import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class PlaceDetailResponseDto {
    private final Long placeId;
    private final String placeName;
    private final Long plcCategoryId;
    private final String category;
    private final Integer reviewCount;
    private final Double reviewAvg;
    private final String address;
    private final List<String> tags;
    private final String businessHour;
    private final String telNo;
    private final String hmpgUrl;
    private final String latitude;
    private final String longitude;
    private final String closedDays;
    private final String price;
    private final String limitInfo;
    private final String description;
    private final String enterPetSize;
    private final Boolean likeStatus;
    private final Integer viewCount;

    private final List<String> images;

    private final List<PhotoReviewSummaryResponseDto> photoReviewList;
    private final List<ReviewSummaryResponseDto> review;

    /**
     * PlaceDetailResponseDto 객체를 생성하는 정적 팩토리 메서드.
     *
     * @param placeInfoDto 장소 정보 DTO
     * @param address 주소 문자열
     * @param tags 태그 리스트
     * @param images 이미지 리스트
     * @param photoReviewSummaryList 사진 리뷰 요약 리스트 (Slice 객체)
     * @param reviewSummaryList 일반 리뷰 요약 리스트
     * @return PlaceDetailResponseDto 객체
     */
    public static PlaceDetailResponseDto of(
            PlaceInfoDto placeInfoDto,
            String address,
            List<String> tags,
            List<String> images,
            Slice<PhotoReviewSummaryResponseDto> photoReviewSummaryList,
            List<ReviewSummaryResponseDto> reviewSummaryList,
            Integer viewCount
    ) {
        return PlaceDetailResponseDto.builder()
                .placeId(placeInfoDto.getPlaceId()) // 장소 ID
                .placeName(placeInfoDto.getPlaceName()) // 장소 이름
                .plcCategoryId(placeInfoDto.getPlaceCategoryId())
                .category(placeInfoDto.getPlaceCategoryName()) // 카테고리
                .reviewCount(placeInfoDto.getReviewCount()) // 리뷰 수
                .reviewAvg(placeInfoDto.getReviewAvg()!= null
                        ? BigDecimal.valueOf(placeInfoDto.getReviewAvg()).setScale(1, RoundingMode.HALF_UP).doubleValue()
                        : null) // 리뷰 평균
                .address(address) // 주소
                .tags(tags) // 태그 리스트
                .businessHour(placeInfoDto.getBusinessHour()) // 영업 시간
                .telNo(placeInfoDto.getTelNo()) // 전화번호
                .hmpgUrl(placeInfoDto.getHmpgUrl()) // 홈페이지 URL
                .latitude(placeInfoDto.getLatitude()) // 위도
                .longitude(placeInfoDto.getLongitude()) // 경도
                .closedDays(placeInfoDto.getClosedDays()) // 휴무일
                .price(placeInfoDto.getPrice()) // 가격 정보
                .limitInfo(placeInfoDto.getLimitInfo()) // 제한 정보
                .description(placeInfoDto.getDescription()) // 설명
                .enterPetSize(placeInfoDto.getEnterPetSize()) // 반려동물 허용 크기
                .images(images) // 이미지 리스트
                .likeStatus(placeInfoDto.getLikeStatus()) // 좋아요 여부
                .photoReviewList(photoReviewSummaryList.getContent().size() >= 4 // 사진 리뷰 4개 이상일 경우
                        ? photoReviewSummaryList.getContent() // 내용을 추가
                        : Collections.emptyList()) // 그렇지 않으면 빈 리스트 반환
                .review(reviewSummaryList) // 일반 리뷰 리스트
                .viewCount(viewCount)
                .build();
    }
}
