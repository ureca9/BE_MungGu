package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.pension.dto.PensionDetailResponseDto;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
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
public class PensionDetailService {

    private final PensionRepository pensionRepository;
    private final AddressService addressService;
    private final ReviewService reviewService;

    /**
     * 주어진 고유 ID를 기준으로 펜션의 상세 정보를 조회합니다.
     *
     * 이 메서드는 주어진 펜션 ID와 연관된 Pension 엔티티, 주소, 리뷰를 조회하고,
     * 이를 바탕으로 PensionDetailResponseDto를 생성하여 반환합니다.
     *
     * @param pensionId 상세 정보를 조회할 펜션의 고유 식별자
     * @return 펜션의 주소, 사진 리뷰 요약, 일반 리뷰 요약을 포함한
     *         PensionDetailResponseDto 인스턴스
     * @throws NotFoundException 제공된 ID에 해당하는 펜션이 존재하지 않을 경우 발생
     */
    @Transactional(readOnly = true)
    public PensionDetailResponseDto getPensionDetail(Long pensionId) {
        Pension pension = getPension(pensionId);
        String address = addressService.getAddress(pensionId, "020");
        List<Review> reviews = reviewService.getReviews(pensionId, "020", 0, 5);

        return getPensionDetailResponseDto(
                pension,
                address,
                reviewService.getPhotoReviewSummaries(pensionId, "020"), // 변경된 호출
                reviewService.getReviewSummaryResponseDtoList(reviews)
        );
    }

    /**
     * 주어진 펜션 ID에 해당하는 Pension 엔티티를 조회합니다.
     * 해당 ID로 Pension 엔티티를 찾을 수 없는 경우 NotFoundException을 발생시킵니다.
     *
     * @param pensionId 조회할 Pension 엔티티의 고유 식별자
     * @return 제공된 펜션 ID에 해당하는 Pension 엔티티
     * @throws NotFoundException 제공된 ID로 Pension 엔티티를 찾을 수 없는 경우 발생
     */
    private Pension getPension(Long pensionId){
        return pensionRepository.findByPensionId(pensionId).
                orElseThrow(() -> NotFoundException.entityNotFound(Long.toString(pensionId)));
    }

    /**
     * PensionDetailResponseDto 생성하는 메서드
     * @param pension 펜션 엔티티
     * @param address 주소 문자열
     * @param photoReviewSummaryList 사진 리뷰 요약 리스트
     * @param reviewSummaryList 일반 리뷰 요약 리스트
     * @return PlaceDetailResponseDto 장소 상세 정보 DTO
     */
    private PensionDetailResponseDto getPensionDetailResponseDto(Pension pension, String address,
                                                                 List<PhotoReviewSummaryResponseDto> photoReviewSummaryList,
                                                                 List<ReviewSummaryResponseDto> reviewSummaryList){
        return PensionDetailResponseDto.builder()
                .pensionId(pension.getPensionId())
                .pensionName(pension.getName())
                .reviewCount(pension.getReviewCount())
                .reviewAvg(pension.getReviewAvg())
                .address(address)
                .tags(pension.getPensionTags().stream()
                        .map(tag -> tag.getTag().getName())
                        .toList())
                .startTime(pension.getStartTime())
                .endTime(pension.getEndTime())
                .telNo(pension.getTelNo())
                .latitude(pension.getLatitude())
                .longitude(pension.getLongitude())
                .description(pension.getPensionDescription())
                .enterPetSize(pension.getEnterPetSize())
                .info(pension.getInfo())
                .introduction(pension.getIntroduction())
                .limitInfo(pension.getPetLimitInfo())
                .images(pension.getPensionFiles().stream()
                            .map(file -> file.getMediaFile().getFileUrl())
                        .toList())
                .photoReviewList(photoReviewSummaryList)
                .review(reviewSummaryList)
                .build();

    }
}
