package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.place.dto.PlaceDetailResponseDto;
import com.meong9.backend.domain.place.dto.PlaceInfoDto;
import com.meong9.backend.domain.place.repository.PlaceFileRepository;
import com.meong9.backend.domain.place.repository.PlaceTagRepository;
import com.meong9.backend.domain.review.service.ReviewService;
import com.meong9.backend.global.utils.RedisKeys;
import com.meong9.backend.global.utils.RedisUtils;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceDetailService {

    private final AddressService addressService;
    private final ReviewService reviewService;
    private final PlaceService placeService;
    private final PlaceTagRepository placeTagRepository;
    private final PlaceFileRepository placeFileRepository;
    private final RedisTemplate<String, String> redisTemplate;


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
    public PlaceDetailResponseDto getPlaceDetail(Long placeId, Long memberId) {
        Pageable pageable = PageRequest.of(0, 5); // 페이지 크기를 5으로 고정
        PlaceInfoDto placeInfoDto = placeService.getPlaceInfoDto(placeId, memberId);

        Double score;
        try {
            score = redisTemplate.opsForZSet().score(
                    RedisKeys.getPlaceWeeklyViewCountKey(placeInfoDto.getPlaceCategoryName(), RedisUtils.formatRelativeToNowDate(1)),
                    String.valueOf(placeId));
        }catch (RedisConnectionException e) {
            log.error("Redis 연결 오류: {}", e.getMessage());
            score = null;
        }

        Integer integerScore = (score != null) ? score.intValue() : null; // score가 null인 경우도 처리
        return PlaceDetailResponseDto.of(
                placeInfoDto,
                addressService.getAddress(placeId, "010"),
                placeTagRepository.findNamesByPlaceId(placeId),
                placeFileRepository.findImagesByPlaceId(placeId),
                reviewService.getPhotoReviewSummaryResponseDtoList(placeId, "010", pageable),
                reviewService.getReviews("010", placeId, pageable).getContent(),
                integerScore
        );
    }

}
