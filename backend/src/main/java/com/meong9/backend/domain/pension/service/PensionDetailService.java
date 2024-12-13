package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.pension.dto.PensionDetailResponseDto;
import com.meong9.backend.domain.pension.repository.PensionFileRepository;
import com.meong9.backend.domain.pension.repository.PensionTagRepository;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PensionDetailService {

    private final AddressService addressService;
    private final ReviewService reviewService;
    private final PensionService pensionService;
    private final PensionFileRepository pensionFileRepository;
    private final PensionTagRepository pensionTagRepository;

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
    public PensionDetailResponseDto getPensionDetail(Long pensionId, Long memberId) {
        Pageable pageable = PageRequest.of(0, 5); // 페이지 크기를 5으로 고정

        return PensionDetailResponseDto.of(
                pensionService.getPensionInfo(pensionId, memberId),
                pensionTagRepository.findTagsByPensionId(pensionId),
                pensionFileRepository.findImagesByPensionId(pensionId),
                addressService.getAddress(pensionId, "020"),
                reviewService.getPhotoReviewSummaryResponseDtoList(pensionId,"020",pageable),
                reviewService.getReviews("020", pensionId, pageable).getContent()
        );
    }



}
