package com.meong9.backend.domain.place.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meong9.backend.domain.place.dto.TopPlaceResponseDto;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.global.utils.CategoryMapper;
import com.meong9.backend.global.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopPlaceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final PlaceRepository placeRepository;


    // ----------------- 공용 메서드 -----------------

    /**
     * 카테고리별 시설의 조회수 상위 9을 반환하는 메서드
     *
     * @param category 시설의 이름
     */
    @Transactional(readOnly = true)
    public List<TopPlaceResponseDto> getTop9PlacesByCategory(String category) {
        // Step 1: Redis 캐시 키 정의
        String categoryName = CategoryMapper.getCategoryName(category);
        String cacheKey = "top_Place:"+ categoryName;


        try {
            // Step 2: Redis에서 데이터 확인
            // (Redis 키(cacheKey 또는 weeklyCountKey)가 존재하지 않는 경우.
            // Redis 키는 존재하지만, 값이 비어 있는 경우(null 반환).
            // 데이터가 만료(TTL)되어 삭제된 경우.
            // Redis에서 캐시 데이터 확인
            // Step 2: Redis에서 데이터 확인
            String jsonData = (String) objectRedisTemplate.opsForValue().get(cacheKey);
            if (jsonData != null) {
                List<TopPlaceResponseDto> result = new ObjectMapper().readValue(
                        jsonData,
                        new TypeReference<List<TopPlaceResponseDto>>() {}
                );
                return result;
            }

            // Step 3: Redis에서 상위 9개 ID 가져오기
            String weeklyCountKey = "place:"+categoryName+":weekly:viewCount" + RedisUtils.formatRelativeToNowDate(1);
            List<Long> topIds = getTop9IdsFromRedis(weeklyCountKey);

            // Redis에서 상위 ID 데이터가 없을 경우
            if (topIds == null || topIds.isEmpty()) {
                return handleRedisFailure(); // 실패 시 대체 로직 실행
            }

            // Step 4: DB에서 추가 정보 조회
            List<TopPlaceResponseDto> placesFromDb = getPlacesFromDb(topIds);

            // Step 5: Redis에 데이터 저장 (다음 날 오전 2시까지 유지)
            objectRedisTemplate.opsForValue().set(
                    cacheKey,
                    new ObjectMapper().writeValueAsString(placesFromDb),
                    RedisUtils.calculateTTLUntil2AM(),
                    TimeUnit.SECONDS
                    
            );
            return placesFromDb;
        }catch (Exception e) {
            // Redis에서 조회 실패 시 예외 처리
            log.error("Failed to fetch top IDs from Redis: {}", e.getMessage(), e);
            return handleRedisFailure(); // 실패 시 대체 로직 실행
        }
    }

// ----------------- 기타 유틸리티 메서드 -----------------

    /**
     * 특정 카테고리의 조회수를 증가시키고 증가된 값을 반환합니다.
     * 조회수는 카테고리별 Sorted Set 구조로 저장되며, 자동으로 정렬됩니다.
     *
     * @param categoryName 카테고리 이름 (예: "공원")
     * @param placeId 조회수를 증가시킬 장소 ID
     * @return 증가 후의 조회수
     */
    public Double incrementCategoryViewCount(String categoryName, Long placeId) {
        String sortedSetKey = "place:" + categoryName + ":viewCount:" + RedisUtils.formatCurrentDate(); // Redis Sorted Set 키 생성
        String placeIdStr = String.valueOf(placeId); // placeId를 String으로 변환

        // Sorted Set에 조회수 증가
        Double viewCount = redisTemplate.opsForZSet().incrementScore(sortedSetKey, placeIdStr, 1);
        redisTemplate.expire(sortedSetKey, Duration.ofDays(7)); // TTL 7일 설정

        // null 처리
        if (viewCount == null) {
            log.warn("Failed to increment score for placeId: {}", placeId);
            // 기본값을 반환하거나, 추가 처리를 수행
            return 0.0;
        }

        // Sorted Set에 조회수 증가
        return viewCount;
    }
    
    /**
     * 탑9 시설의 ids를 리스트 형식으로 반환합니다.
     *
     * @param redisKey 탑9 시설 ID
     * @return 탑 9 시설의 ids
     */
    private List<Long> getTop9IdsFromRedis(String redisKey) {
        Set<String> topIds = redisTemplate.opsForZSet().reverseRange(redisKey, 0, 8);

        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }

        return topIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * DB에서 주어진 ID 목록에 해당하는 시설 데이터를 조회하고,
     * ID 순서를 유지하여 정렬된 결과를 반환합니다.
     *
     * @param ids 조회할 시설의 ID 목록
     * @return ID 순서대로 정렬된 {@link TopPlaceResponseDto} 리스트
     */
    private List<TopPlaceResponseDto> getPlacesFromDb(List<Long> ids) {
        // 1. DB에서 ID 목록으로 데이터를 조회
        List<TopPlaceResponseDto> places = placeRepository.findPlaceTop(ids, "010");

        // 2. 조회된 데이터를 Map으로 변환 (key: placeId)
        Map<Long, TopPlaceResponseDto> placeMap = places.stream()
                .collect(Collectors.toMap(TopPlaceResponseDto::getPlaceId, dto -> dto));

        // 3. ids 순서대로 정렬
        return ids.stream()
                .map(placeMap::get) // ids 순서에 따라 Map에서 값 가져오기
                .filter(Objects::nonNull) // 없는 ID는 제외
                .toList(); // 최종 결과를 List로 변환
    }

    /**
     * Redis 조회 실패 시 실행되는 백업 로직으로, DB에서 reviewCount 기준 상위 9개의
     * 시설 데이터를 조회하여 반환합니다.
     *
     * @return 상위 9개의 {@link TopPlaceResponseDto} 리스트
     */
    private List<TopPlaceResponseDto> handleRedisFailure() {
        // Redis 실패 시 DB에서 상위 9개 데이터를 조회
        log.warn("Redis failure occurred. Fetching top places from DB as fallback.");

        // Pageable 설정 (첫 번째 페이지, 9개 데이터)
        Pageable pageable = PageRequest.of(0, 9);

        // DB에서 상위 9개 데이터 조회
        Page<TopPlaceResponseDto> topPlacesPage = placeRepository.findTopPlacesByReviewCount("010", pageable);

        // 결과 반환
        return topPlacesPage.getContent();
    }
}
