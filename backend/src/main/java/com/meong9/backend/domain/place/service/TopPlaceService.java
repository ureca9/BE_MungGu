package com.meong9.backend.domain.place.service;

import com.meong9.backend.domain.address.entity.Address;
import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.place.dto.TopPlaceResponseDto;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.entity.PlaceFeature;
import com.meong9.backend.domain.place.entity.TopPlace;
import com.meong9.backend.domain.place.entity.id.PlaceFeatureId;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.place.repository.TopPlaceRepository;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import com.meong9.backend.global.topFeature.repository.TopFeatureRepository;
import com.meong9.backend.global.utils.CategoryMapper;
import com.meong9.backend.global.utils.TagToFeatureMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopPlaceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PlaceRepository placeRepository;
    private final TopPlaceRepository topPlaceRepository;
    private final AddressService addressService;
    private final TopFeatureRepository topFeatureRepository;

    // ----------------- 공용 메서드 -----------------

    /**
     * 카테고리별 시설의 조회수 상위 9을 반환하는 메서드
     *
     * @param category 시설의 이름
     */
    @Transactional(readOnly = true)
    public List<TopPlaceResponseDto> getTop9PlacesByCategory(String category) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);


        // Step 1: TopPlace 데이터 조회
        log.info("Category: {}", CategoryMapper.getCategoryName(category));

        List<TopPlaceResponseDto> places = topPlaceRepository.findTop9PlacesByDateRangeAndCategory(
                startDate.getYear(),
                startDate.getMonthValue(),
                startDate.getDayOfMonth(),
                endDate.getYear(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth(),
                category,
                PageRequest.of(0, 9)
        ).getContent();

        // Step 2: placeIds 추출
        List<Long> placeIds = places.stream()
                .map(TopPlaceResponseDto::getPlaceId)
                .toList();

        // Step 3: 대표 이미지 조회
        List<Object[]> images = topPlaceRepository.findRepresentativeImagesByPlaceIds(placeIds);

        // Step 4: 이미지 매핑
        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (String) obj[1]));

        places.forEach(p -> p.setPlaceImageUrl(imageMap.get(p.getPlaceId())));
        return places;
    }

    /**
     * 매일 자정에 실행되어 각 카테고리의 상위 TopPlace 데이터를 처리합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void aggregateDailyTopPlaces() {
        List<String> categoryList = CategoryMapper.getAllCategoryNames();

        for (String category : categoryList) {
            aggregateTopPlaces(category);
        }
        // 모든 펜션의 조회수 데이터를 삭제합니다.
        clearAllPlaceViewCounts();
    }

    /**
     * 특정 카테고리의 상위 TopPlace 데이터를 Redis에서 조회하여 데이터베이스에 저장합니다.
     *
     * @param categoryName 카테고리 이름
     */
    @Transactional
    public void aggregateTopPlaces(String categoryName) {
        List<TopPlace> topPlaces = fetchTopPlacesFromRedis(categoryName);
        saveTopPlacesToDatabase(topPlaces);
    }

    /**
     * Redis에서 특정 카테고리의 TopPlace 데이터를 조회하고 리스트로 반환합니다.
     *
     * @param category 카테고리 이름
     * @return TopPlace 객체의 리스트
     */
    @Transactional
    public List<TopPlace> fetchTopPlacesFromRedis(String category) {
        LocalDate today = LocalDate.now();
        Set<ZSetOperations.TypedTuple<String>> topViewPlaces = getTopViewPlacesFromRedis(category);

        if (topViewPlaces == null || topViewPlaces.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Place> placeMap = getPlaceMapFromDatabase(topViewPlaces);
        return buildTopPlacesList(topViewPlaces, placeMap, today, category);
    }

    // ----------------- 핵심 비즈니스 로직 메서드 -----------------

    /**
     * TopPlace 리스트를 생성합니다.
     *
     * @param topViewPlaces Redis에서 가져온 조회수 데이터
     * @param placeMap 데이터베이스에서 조회된 Place 객체의 Map
     * @param today 현재 날짜
     * @param category 카테고리 이름
     * @return 생성된 TopPlace 객체의 리스트
     */
    private List<TopPlace> buildTopPlacesList(Set<ZSetOperations.TypedTuple<String>> topViewPlaces,
                                              Map<Long, Place> placeMap,
                                              LocalDate today,
                                              String category) {

        List<TopPlace> topPlaces = new ArrayList<>();
        int rank = 1;

        // 모든 placeId를 수집
        List<Long> placeIds = placeMap.keySet().stream().toList();

        // 배치 조회로 주소 데이터 가져오기
        Map<Long, Address> addressMap = addressService.getAddressesForPensionsOrPlaces(placeIds, "010");

        for (ZSetOperations.TypedTuple<String> place : topViewPlaces) {
            Long placeId = Long.valueOf(place.getValue());
            Double score = place.getScore();
            Place placeEntity = placeMap.get(placeId);

            if (placeEntity == null) {
                continue;
            }

            List<Long> tagIds = placeEntity.getPlaceTags().stream()
                    .map(placeTag -> placeTag.getTag().getTagId())
                    .collect(Collectors.toList());

            TopFeature topFeature = createTopFeatureFromTags(tagIds);

            // 배치로 가져온 주소 사용
            Address address = addressMap.get(placeId);

            TopPlace topPlace = createTopPlace(placeEntity, score, rank++, CategoryMapper.getCategoryId(category), today, address);
            createAndAddPlaceFeature(topPlace, placeEntity, topFeature);

            topPlaces.add(topPlace);
        }

        return topPlaces;
    }


    /**
     * TopPlace 객체를 생성합니다.
     *
     * @param placeEntity Place 객체
     * @param score Redis에서 가져온 조회수
     * @param rank 순위
     * @param category 카테고리 이름
     * @param today 현재 날짜
     * @param address Address 객체
     * @return 생성된 TopPlace 객체
     */
    private TopPlace createTopPlace(Place placeEntity, Double score, int rank, String category, LocalDate today, Address address) {
        return TopPlace.builder()
                .placeId(placeEntity.getPlaceId())
                .placeName(placeEntity.getName())
                .reviewCount(placeEntity.getReviewCount())
                .reviewAvg(BigDecimal.valueOf(placeEntity.getReviewAvg()))
                .likeCount(placeEntity.getLikeCount())
                .category(category)
                .province(address != null ? address.getProvince() : null)
                .cityDistrict(address != null ? address.getCityDistrict() : null)
                .subDistrict(address != null ? address.getSubDistrict() : null)
                .viewCount(score.longValue())
                .rank(rank)
                .year(today.getYear())
                .month(today.getMonthValue())
                .date(today.getDayOfMonth())
                .placeFeatures(new ArrayList<>())
                .build();
    }

    /**
     * PlaceFeature 객체를 생성하고 TopPlace에 추가합니다.
     *
     * @param topPlace TopPlace 객체
     * @param placeEntity Place 객체
     * @param topFeature TopFeature 객체
     */
    private void createAndAddPlaceFeature(TopPlace topPlace, Place placeEntity, TopFeature topFeature) {
        topFeatureRepository.save(topFeature);

        PlaceFeature placeFeature = new PlaceFeature(
                new PlaceFeatureId(placeEntity.getPlaceId(), topFeature.getTopFeatureId()),
                topPlace,
                topFeature
        );

        topPlace.getPlaceFeatures().add(placeFeature);
    }

// ----------------- 데이터 접근 및 유틸리티 메서드 -----------------

    /**
     * Redis에서 특정 카테고리의 조회수 상위 데이터를 가져옵니다.
     *
     * @param category 카테고리 이름 (예: "공원", "레스토랑")
     * @return 조회수 상위 20개의 ZSetOperations.TypedTuple 데이터
     */
    private Set<ZSetOperations.TypedTuple<String>> getTopViewPlacesFromRedis(String category) {
        String sortedSetKey = "place:category:" + category; // Redis Sorted Set 키 생성
        return redisTemplate.opsForZSet().reverseRangeWithScores(sortedSetKey, 0, 19); // 상위 20개 데이터 반환
    }

    /**
     * Redis에서 가져온 placeId를 기반으로 Place 데이터를 데이터베이스에서 조회하고 Map으로 변환합니다.
     *
     * @param topViewPlaces Redis에서 가져온 조회수 데이터 (placeId 및 score 포함)
     * @return placeId를 키로 갖는 Place 객체의 Map
     */
    private Map<Long, Place> getPlaceMapFromDatabase(Set<ZSetOperations.TypedTuple<String>> topViewPlaces) {
        // Redis에서 가져온 placeId를 Long 타입으로 변환
        List<Long> placeIds = topViewPlaces.stream()
                .map(tuple -> Long.valueOf(tuple.getValue()))
                .collect(Collectors.toList());

        // Place 데이터를 데이터베이스에서 조회
        List<Place> places = placeRepository.findByPlaceIds(placeIds);

        // placeId를 키로 하는 Map 생성
        return places.stream().collect(Collectors.toMap(Place::getPlaceId, place -> place));
    }

    /**
     * 생성된 TopPlace 리스트를 데이터베이스에 저장합니다.
     *
     * @param topPlaces 저장할 TopPlace 리스트
     */
    private void saveTopPlacesToDatabase(List<TopPlace> topPlaces) {
        topPlaceRepository.saveAll(topPlaces); // 리스트 전체를 저장
    }

// ----------------- 기타 유틸리티 메서드 -----------------

    /**
     * 주어진 태그 ID 리스트를 기반으로 TopFeature 객체를 생성합니다.
     *
     * @param tagIds 태그 ID 리스트 (Long 타입)
     *               - 각 태그 ID는 특정 TopFeature 필드를 설정하는 데 사용됩니다.
     * @return 설정된 TopFeature 객체
     *         - 태그 ID에 해당하는 필드가 true로 설정된 객체가 반환됩니다.
     */
    private TopFeature createTopFeatureFromTags(List<Long> tagIds) {
        TopFeature topFeature = new TopFeature(); // 빈 TopFeature 객체 생성

        // 태그 ID 리스트를 순회하며 각 태그 ID에 해당하는 설정 적용
        for (Long tagId : tagIds) {
            TagToFeatureMapping.applyFeature(tagId, topFeature); // 매핑된 설정 적용
        }

        return topFeature; // 설정이 완료된 TopFeature 객체 반환
    }

    /**
     * 특정 카테고리의 조회수를 증가시키고 증가된 값을 반환합니다.
     * 조회수는 카테고리별 Sorted Set 구조로 저장되며, 자동으로 정렬됩니다.
     *
     * @param categoryName 카테고리 이름 (예: "공원")
     * @param placeId 조회수를 증가시킬 장소 ID
     * @return 증가 후의 조회수
     */
    public Double incrementCategoryViewCount(String categoryName, Long placeId) {
        String sortedSetKey = "place:category:" + categoryName; // Redis Sorted Set 키 생성
        String placeIdStr = String.valueOf(placeId); // placeId를 String으로 변환

        // Sorted Set에 조회수 증가
        return redisTemplate.opsForZSet().incrementScore(sortedSetKey, placeIdStr, 1); // 증가된 조회수 반환
    }

    /**
     * 모든 펜션의 조회수 데이터를 삭제합니다.
     */
    private void clearAllPlaceViewCounts() {
        List<String> categoryList = CategoryMapper.getAllCategoryNames();

        for (String category : categoryList) {
            redisTemplate.delete("place:category:" + category);
        }
    }
}
