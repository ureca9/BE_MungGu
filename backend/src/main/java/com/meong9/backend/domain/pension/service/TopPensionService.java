package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.address.entity.Address;
import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.pension.dto.TopPensionResponseDto;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.entity.PensionFeature;
import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.domain.pension.entity.id.PensionFeatureId;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.pension.repository.TopPensionRepository;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import com.meong9.backend.global.topFeature.repository.TopFeatureRepository;
import com.meong9.backend.global.utils.RedisUtils;
import com.meong9.backend.global.utils.TagToFeatureMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopPensionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    private final PensionRepository pensionRepository;
    private final TopPensionRepository topPensionRepository;
    private final AddressService addressService;
    private final TopFeatureRepository topFeatureRepository;
    private final RoomService roomService;

    // ----------------- 핵심 로직 -----------------

    @Transactional(readOnly = true)
    public List<TopPensionResponseDto> getTop9PensionsByCategory() {

        String cacheKey = "top_Pension";

        // Step 1: Redis에서 데이터 읽기
        Object cachedData = objectRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return (List<TopPensionResponseDto>) cachedData;
        }

        // Step 2: DB에서 데이터 조회
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        List<TopPensionResponseDto> pensions = topPensionRepository.findTop9PensionsByDateRangeAndCategory(
                startDate.getYear(),
                startDate.getMonthValue(),
                startDate.getDayOfMonth(),
                endDate.getYear(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth(),
                PageRequest.of(0, 9)
        ).getContent();

        /// Step 3: pensionIds 추출
        List<Long> pensionIds = pensions.stream()
                .map(TopPensionResponseDto::getPensionId)
                .toList();

        // Step 4: 대표 이미지 조회
        List<Object[]> images = topPensionRepository.findRepresentativeImagesByPensionIds(pensionIds);

        // Step 5: 이미지 매핑
        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (String) obj[1]));

        // 이미지 URL을 DTO에 매핑
        pensions.forEach(p -> p.setPensionImageUrl(imageMap.get(p.getPensionId())));

        // Step 6: Redis에 데이터 저장
        long ttlUntil2AM = RedisUtils.calculateTTLUntil2AM();
        objectRedisTemplate.opsForValue().set(cacheKey, pensions, ttlUntil2AM, TimeUnit.SECONDS);

        return pensions;
    }

    /**
     * 매일 자정에 실행되어 상위 TopPension 데이터를 처리합니다.
     */
//    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void aggregateDailyTopPensions() {
        List<TopPension> topPensions = fetchAllPensionsFromRedis();
        topPensionRepository.saveAll(topPensions);
        // 1. 레디스에서 한번에 다 가져온 다음 100건씩 배치 인서트(JDBC)

        // 2. 레디스에서 100건씩 끊어서 가져온 다음 100건씩 배치 인서트

        // 모든 펜션의 조회수 데이터를 삭제합니다.
        clearAllPensionViewCounts();
    }

    /**
     * Redis에서 상위 20개의 Pension 데이터를 조회하고 리스트로 반환합니다.
     *
     * @return TopPension 객체의 리스트
     */
    @Transactional
    public List<TopPension> fetchAllPensionsFromRedis() {
        LocalDate today = LocalDate.now();

        // Redis에서 조회수 상위 20개의 데이터(value와 score 포함)를 가져오기
        Set<ZSetOperations.TypedTuple<String>> topViewPensions = redisTemplate.opsForZSet()
                .reverseRangeWithScores("pension:viewCount", 0, -1);

        if (topViewPensions == null || topViewPensions.isEmpty()) {
            return new ArrayList<>(); // 데이터가 없을 경우 빈 리스트 반환
        }

        // 여기서 100건 끊어서 가져와야 할듯?
        // Redis에서 가져온 pensionId 기반으로 Pension 데이터를 조회
        Map<Long, Pension> pensionMap = getPensionMapFromDatabase(topViewPensions);

        // TopPension 리스트 생성
        return buildTopPensionsList(topViewPensions, pensionMap, today);
    }

    /**
     * TopPension 리스트를 생성합니다.
     *
     * @param topViewPensions Redis에서 가져온 조회수 데이터
     * @param pensionMap 데이터베이스에서 조회된 Pension 객체의 Map
     * @param today 현재 날짜
     * @return 생성된 TopPension 객체의 리스트
     */
    private List<TopPension> buildTopPensionsList(Set<ZSetOperations.TypedTuple<String>> topViewPensions,
                                                  Map<Long, Pension> pensionMap,
                                                  LocalDate today) {
        List<Long> pensionIds = pensionMap.keySet().stream().toList();

        // Address와 평균 객실 가격을 한 번에 조회
        Map<Long, Address> addressMap = addressService.getAddressesForPensionsOrPlaces(pensionIds, "020");
        Map<Long, BigDecimal> roomPriceAvgMap = roomService.findAveragePricesByPensionIds(pensionIds);

        List<TopPension> topPensions = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> pension : topViewPensions) {
            Long pensionId = Long.valueOf(pension.getValue());
            Double score = pension.getScore();

            Pension pensionEntity = pensionMap.get(pensionId);
            if (pensionEntity == null) {
                continue;
            }

            Address address = addressMap.get(pensionId);
            BigDecimal roomPriceAvg = roomPriceAvgMap.getOrDefault(pensionId, BigDecimal.ZERO);

            // TopPension 생성
            TopPension topPension = createTopPension(pensionEntity, score, rank++, today, address, roomPriceAvg);

            // 연관 데이터 설정
            List<Long> tagIds = pensionEntity.getPensionTags().stream()
                    .map(tag -> tag.getTag().getTagId())
                    .collect(Collectors.toList());
            TopFeature topFeature = createTopFeatureFromTags(tagIds);
            createAndAddPensionFeature(topPension, pensionEntity, topFeature);

            topPensions.add(topPension);
        }

        return topPensions;
    }

    // ----------------- 데이터 생성 및 저장 -----------------

    /**
     * TopPension 객체를 생성합니다.
     *
     * @param pensionEntity Pension 객체
     * @param score Redis에서 가져온 조회수
     * @param rank 순위
     * @param today 현재 날짜
     * @param address Address 객체
     * @param roomPriceAvg 평균 객실 가격
     * @return 생성된 TopPension 객체
     */
    private TopPension createTopPension(Pension pensionEntity, Double score, int rank, LocalDate today, Address address, BigDecimal roomPriceAvg) {
        return TopPension.builder()
                .pensionId(pensionEntity.getPensionId())
                .pensionName(pensionEntity.getName())
                .reviewCount(pensionEntity.getReviewCount())
                .reviewAvg(BigDecimal.valueOf(pensionEntity.getReviewAvg()))
                .likeCount(pensionEntity.getLikeCount())
                .province(address != null ? address.getProvince() : null)
                .cityDistrict(address != null ? address.getCityDistrict() : null)
                .subDistrict(address != null ? address.getSubDistrict() : null)
                .viewCount(score.longValue())
                .rank(rank)
                .year(today.getYear())
                .month(today.getMonthValue())
                .date(today.getDayOfMonth())
                .roomPriceAvg(roomPriceAvg)
                .pensionFeatures(new ArrayList<>())
                .build();
    }

    /**
     * 주어진 태그 ID 리스트를 기반으로 TopFeature 객체를 생성합니다.
     *
     * @param tagIds 태그 ID 리스트
     * @return 생성된 TopFeature 객체
     */
    private TopFeature createTopFeatureFromTags(List<Long> tagIds) {
        TopFeature topFeature = new TopFeature();
        for (Long tagId : tagIds) {
            TagToFeatureMapping.applyFeature(tagId, topFeature);
        }
        return topFeature;
    }

    /**
     * PensionFeature 객체를 생성하고 TopPension에 추가합니다.
     *
     * @param topPension TopPension 객체
     * @param pensionEntity Pension 객체
     * @param topFeature TopFeature 객체
     */
    private void createAndAddPensionFeature(TopPension topPension, Pension pensionEntity, TopFeature topFeature) {
        topFeatureRepository.save(topFeature); // TopFeature 저장

        PensionFeature pensionFeature = new PensionFeature(
                new PensionFeatureId(pensionEntity.getPensionId(), topFeature.getTopFeatureId()),
                topFeature,
                topPension
        );

        topPension.getPensionFeatures().add(pensionFeature);
    }

    // ----------------- 데이터 접근 및 유틸리티 메서드 -----------------

    /**
     * Redis에서 특정 펜션 데이터를 조회하여 Map으로 변환합니다.
     *
     * @param topViewPensions Redis에서 가져온 조회수 데이터
     * @return pensionId를 키로 하는 Pension Map
     */
    private Map<Long, Pension> getPensionMapFromDatabase(Set<ZSetOperations.TypedTuple<String>> topViewPensions) {
        List<Long> pensionIds = topViewPensions.stream()
                .map(tuple -> {
                    try {
                        return Long.valueOf(Objects.requireNonNull(tuple.getValue(), "pensionId cannot be Null"));
                    } catch (NumberFormatException e) {
                        log.warn("유효하지 않은 pensionId입니다.: {}", tuple.getValue());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Pension> pensions = pensionRepository.findByPensionIds(pensionIds);
        return pensions.stream().collect(Collectors.toMap(Pension::getPensionId, pension -> pension));
    }

    /**
     * 펜션의 조회수를 증가시키고 증가된 값을 반환합니다.
     *
     * @param pensionId 조회수를 증가시킬 펜션 ID
     * @return 증가 후의 조회수
     */
    public Double incrementPensionViewCount(Long pensionId) {
        String sortedSetKey = "pension:viewCount";
        String pensionIdStr = String.valueOf(pensionId);
        // Sorted Set에 조회수 증가
        return redisTemplate.opsForZSet().incrementScore(sortedSetKey, pensionIdStr, 1);
    }

    /**
     * 모든 펜션의 조회수 데이터를 삭제합니다.
     */
    private void clearAllPensionViewCounts() {
        String sortedSetKey = "pension:viewCount";
        // Sorted Set 키 삭제
        redisTemplate.delete(sortedSetKey);
    }

}
