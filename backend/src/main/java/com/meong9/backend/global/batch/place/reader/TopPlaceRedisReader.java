package com.meong9.backend.global.batch.place.reader;

import com.meong9.backend.domain.address.entity.Address;
import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.global.batch.place.dto.RedisTopPlaceDto;
import com.meong9.backend.global.utils.CategoryMapper;
import com.meong9.backend.global.utils.RedisKeys;
import com.meong9.backend.global.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopPlaceRedisReader implements ItemReader<RedisTopPlaceDto> {

    private final RedisTemplate<String, String> redisTemplate;
    private final PlaceRepository placeRepository;
    private final AddressService addressService;

    private static final int PAGE_SIZE = 100;
    private int currentIndex = 0;
    private int globalRank = 1; // 전역 rank 변수
    private Iterator<RedisTopPlaceDto> iterator;

    private final List<String> categoryIds = CategoryMapper.getAllCategoryIds();
    private int categoryIndex = 0;


    @Override
    public RedisTopPlaceDto read() {
        if (iterator == null || !iterator.hasNext()) {
            loadNextPage();
        }

        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }

    private void loadNextPage() {
        // 모든 카테고리를 순환하며 Redis 데이터를 처리
        while (categoryIndex < categoryIds.size()) {
            String currentCategoryId = categoryIds.get(categoryIndex);
            String categoryName = CategoryMapper.getCategoryName(currentCategoryId);

            // Redis에서 데이터 조회 (name으로 저장된 key 사용)
            String redisKey = RedisKeys.getPlaceDailyViewCountKey(categoryName, RedisUtils.formatRelativeToNowDate(1));

            Set<ZSetOperations.TypedTuple<String>> redisData = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(redisKey, currentIndex, currentIndex + PAGE_SIZE - 1);

            if (redisData == null || redisData.isEmpty()) {
                // 현재 카테고리 데이터가 없으면 다음 카테고리로 이동
                categoryIndex++;
                currentIndex = 0;
                continue;
            }

            // Place ID 리스트 추출
            List<Long> placeIds = redisData.stream()
                    .map(tuple -> Long.valueOf(tuple.getValue()))
                    .toList();

            // 배치로 Place, Address, 평균 가격 조회
            Map<Long, Place> placeMap = placeRepository.findByPlaceIds(placeIds)
                    .stream()
                    .collect(Collectors.toMap(Place::getPlaceId, p -> p));

            Map<Long, Address> addressMap = addressService.getAddressesForPensionsOrPlaces(placeIds, "010");

            // Redis 데이터와 병합된 정보를 DTO로 변환
            List<RedisTopPlaceDto> dtoList = new ArrayList<>();
            for (ZSetOperations.TypedTuple<String> tuple : redisData) {
                Long placeId = Long.valueOf(tuple.getValue());
                Double score = tuple.getScore();

                Place place = placeMap.get(placeId);
                Address address = addressMap.get(placeId);

                if (place != null) {
                    RedisTopPlaceDto dto = new RedisTopPlaceDto(
                            place.getPlaceId(),
                            score,
                            globalRank++, // 전역 rank 사용
                            place.getName(),
                            place.getReviewCount(),
                            place.getReviewAvg(),
                            place.getLikeCount(),
                            address != null ? address.getProvince() : null,
                            address != null ? address.getCityDistrict() : null,
                            address != null ? address.getSubDistrict() : null,
                            place.getPlaceTags().stream()
                                    .map(tag -> tag.getTag().getTagId())
                                    .collect(Collectors.toList()),
                            currentCategoryId // ID 저장
                    );
                    dtoList.add(dto); // DTO 추가
                }
            }

            iterator = dtoList.iterator();
            currentIndex += PAGE_SIZE;

            // 데이터가 있으면 로드 완료
            if (!dtoList.isEmpty()) {
                return;
            }

            // 현재 카테고리 데이터가 끝나면 다음 카테고리로 이동
            categoryIndex++;
            currentIndex = 0;
        }

        // 모든 카테고리가 처리되었으면 종료
        iterator = null;
    }
}
