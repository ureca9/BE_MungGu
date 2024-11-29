package com.meong9.backend.domain.review.service;

import com.meong9.backend.domain.review.dto.FileResponseDto;
import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * 특정 장소에 연결된 리뷰를 페이징하여 조회하는 메서드
     *
     * @param Id 조회할 장소 또는 펜션의 ID
     * @param type    장소 유형 ("시설" 또는 "펜션")
     * @param pageNum 조회할 페이지 번호 (0부터 시작)
     * @param page    한 페이지당 표시할 리뷰 수
     * @return 페이징 처리된 리뷰 리스트
     */
    public List<Review> getReviews(Long Id, String type, int pageNum, int page) {
        Pageable pageable = PageRequest.of(pageNum, page);
        return reviewRepository.findReviewsByPlaceId(Id, type, pageable);
    }


    /**
     * 리뷰를 바탕으로 ReviewSummaryResponseDto 리스트를 생성하는 메서드
     * @param reviews 리뷰 리스트
     * @return ReviewSummaryResponseDto 리스트
     */
    public List<ReviewSummaryResponseDto> getReviewSummaryResponseDtoList(List<Review> reviews) {
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

    /**
     * 특정 장소의 사진 리뷰 요약 리스트를 조회하는 메서드
     */
    public List<PhotoReviewSummaryResponseDto> getPhotoReviewSummaries(Long placeId, String type) {
        return reviewRepository.findPhotoReviewSummaries(placeId, type);
    }
}


