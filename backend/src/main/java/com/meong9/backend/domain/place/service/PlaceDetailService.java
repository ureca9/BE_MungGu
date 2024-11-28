package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.place.dto.PlaceDetailResponseDto;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.review.dto.FileResponseDto;
import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceDetailService {

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;

    /**
     * 특정 장소의 상세 정보를 조회하는 서비스 메서드
     * @param placeId 장소 ID
     * @return PlaceDetailResponseDto 장소 상세 정보 DTO
     */
    @Transactional(readOnly = true)
    public PlaceDetailResponseDto getPlaceDetail(Long placeId) {
        Place place = getPlace(placeId);
        String address = getAddress(placeId, "010");
        List<Review> reviews = getReviews(placeId, "010");

        return buildPlaceDetailResponseDto(place, address, reviews);
    }

    /**
     * 장소 정보를 조회하는 메서드
     * @param placeId 장소 ID
     * @return Place 엔티티
     */
    private Place getPlace(Long placeId) {
        return placeRepository.findPlaceWithDetails(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found with ID: " + placeId));
    }

    /**
     * 장소에 연결된 주소 정보를 조회하는 메서드
     * @param placeId 장소 ID
     * @return 주소 문자열 (없을 경우 "주소 정보 없음")
     */
    private String getAddress(Long placeId, String type) {
        return plcPenAddressRepository.findPlcPenAddressWithAddress(type, placeId)
                .map(addr -> addr.getAddress().getAddress())
                .orElse("주소 정보 없음");
    }

    /**
     * 특정 장소에 연결된 리뷰 리스트를 조회하는 메서드
     * @param placeId 장소 ID
     * @return 리뷰 리스트
     */
    private List<Review> getReviews(Long placeId, String type) {
        return reviewRepository.findReviewsByPlaceId(placeId, type);
    }

    /**
     * PlaceDetailResponseDto를 생성하는 메서드
     * @param place 장소 엔티티
     * @param address 주소 문자열
     * @param reviews 리뷰 리스트
     * @return PlaceDetailResponseDto 장소 상세 정보 DTO
     */
    private PlaceDetailResponseDto buildPlaceDetailResponseDto(Place place, String address, List<Review> reviews) {
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
                .images(place.getPlaceFiles().stream()
                        .map(file -> file.getMediaFile().getFileUrl())
                        .collect(Collectors.toList()))
                .photoReviewList(buildPhotoReviewSummaryDtoList(reviews))
                .review(buildReviewSummaryDtoList(reviews))
                .build();
    }

    /**
     * 리뷰를 바탕으로 PhotoReviewSummaryDto 리스트를 생성하는 메서드
     * @param reviews 리뷰 리스트
     * @return PhotoReviewSummaryDto 리스트
     */
    private List<PhotoReviewSummaryResponseDto> buildPhotoReviewSummaryDtoList(List<Review> reviews) {
        return reviews.stream()
                .filter(review -> !review.getReviewFiles().isEmpty())
                .map(review -> PhotoReviewSummaryResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .representativeImageUrl(review.getReviewFiles().get(0).getFile().getFileUrl())
                        .photoReviewCount(review.getReviewFiles().size())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 리뷰를 바탕으로 ReviewSummaryDto 리스트를 생성하는 메서드
     * @param reviews 리뷰 리스트
     * @return ReviewSummaryDto 리스트
     */
    private List<ReviewSummaryResponseDto> buildReviewSummaryDtoList(List<Review> reviews) {
        return reviews.stream()
                .map(review -> ReviewSummaryResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .profileImageUrl(null) // Profile 이미지가 별도로 필요하면 추가
                        .content(review.getContent())
                        .score(review.getScore().doubleValue())
                        .visitDate(review.getVisitDate().toString())
                        .createdAt(review.getCreatedAt().toString())
                        .modifiedAt(review.getModifiedAt().toString())
                        .nickname(review.getNickname())
                        .file(review.getReviewFiles().stream()
                                .map(file -> FileResponseDto.builder()
                                        .mediaFileId(file.getFile().getMediaFileId())
                                        .fileType(file.getFile().getFileType().name())
                                        .fileUrl(file.getFile().getFileUrl())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
