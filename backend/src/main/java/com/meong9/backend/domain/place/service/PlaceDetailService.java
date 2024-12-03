package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.place.dto.PlaceDetailResponseDto;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceDetailService {

    private final PlaceRepository placeRepository;
    private final AddressService addressService;
    private final ReviewService reviewService;


    /**
     * 주어진 ID를 기준으로 특정 장소의 상세 정보를 조회합니다.
     * 이 메서드는 장소 엔티티, 해당 주소, 관련 리뷰,
     * 사진 리뷰 요약 정보를 수집하여 상세 응답 DTO를 생성합니다.
     *
     * @param placeId 상세 정보 조회를 요청한 장소의 고유 식별자
     * @return PlaceDetailResponseDto의 인스턴스로,
     *         해당 장소의 주소, 리뷰, 사진 리뷰 요약 정보를 포함한 상세 정보를 반환합니다.
     */
    @Transactional(readOnly = true)
    public PlaceDetailResponseDto getPlaceDetail(Long placeId) {
        Place place = getPlace(placeId);
        String address = addressService.getAddress(placeId, "010");
        List<Review> reviews = reviewService.getReviews(placeId, "010",0 ,5);

        return getPlaceDetailResponseDto(
                place,
                address,
                reviewService.getPhotoReviewSummaries(placeId, "010"),
                reviewService.getReviewSummaryResponseDtoList(reviews)
        );
    }

    /**
     * 주어진 장소 ID에 해당하는 Place 엔티티를 조회합니다.
     * 해당 ID로 Place 엔티티를 찾을 수 없는 경우 NotFoundException을 발생시킵니다.
     * @param placeId 장소 ID
     * @return 제공된 장소 ID에 해당하는 Place 엔티티
     * @throws NotFoundException 제공된 ID로 Place 엔티티를 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public Place getPlace(Long placeId) {
        return placeRepository.findPlaceWithDetails(placeId)
                .orElseThrow(()->NotFoundException.entityNotFound("시설"));
    }

    /**
     * PlaceDetailResponseDto를 생성하는 메서드
     * @param place 장소 엔티티
     * @param address 주소 문자열
     * @param photoReviewSummaryList 사진 리뷰 요약 리스트
     * @param reviewSummaryList 일반 리뷰 요약 리스트
     * @return PlaceDetailResponseDto 장소 상세 정보 DTO
     */
    private PlaceDetailResponseDto getPlaceDetailResponseDto(Place place, String address,
                                           List<PhotoReviewSummaryResponseDto> photoReviewSummaryList,
                                           List<ReviewSummaryResponseDto> reviewSummaryList) {
        return PlaceDetailResponseDto.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getName())
                .category(place.getPlcCategory().getName())
                .reviewCount(place.getReviewCount())
                .reviewAvg(place.getReviewAvg())
                .address(address)
                .tags(place.getPlaceTags().stream()
                        .map(tag -> tag.getTag().getName())
                        .toList())
                .businessHour(place.getBusinessHour())
                .telNo(place.getTelNo())
                .hmpgUrl(place.getHmpgUrl())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .closedDays(place.getClosedDays())
                .price(place.getPriceContent())
                .limitInfo(place.getPetLimitInfo())
                .description(place.getPlcDescription())
                .enterPetSize(place.getEnterPetSize())
                .images(place.getPlaceFiles().stream()
                        .map(file -> file.getMediaFile().getFileUrl())
                        .toList())
                .photoReviewList(photoReviewSummaryList)
                .review(reviewSummaryList)
                .build();
    }
}
