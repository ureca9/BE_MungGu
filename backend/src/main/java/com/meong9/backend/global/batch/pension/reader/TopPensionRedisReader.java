package com.meong9.backend.global.batch.pension.reader;

import com.meong9.backend.domain.address.entity.Address;
import com.meong9.backend.domain.address.service.AddressService;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.pension.service.RoomService;
import com.meong9.backend.global.batch.pension.dto.RedisTopPensionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopPensionRedisReader implements ItemReader<RedisTopPensionDto> {

    private final RedisTemplate<String, String> redisTemplate;
    private final PensionRepository pensionRepository;
    private final AddressService addressService;
    private final RoomService roomService;

    private static final int PAGE_SIZE = 100;
    private int currentIndex = 0;
    private int globalRank = 1; // 페이지를 넘어가도 유지될 전역 rank 변수
    private Iterator<RedisTopPensionDto> iterator;

    @Override
    public RedisTopPensionDto read() {
        if (iterator == null || !iterator.hasNext()) {
            loadNextPage();
        }

        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }

    private void loadNextPage() {
        // Step 1: Redis에서 데이터 조회
        Set<ZSetOperations.TypedTuple<String>> redisData = redisTemplate.opsForZSet()
                .reverseRangeWithScores("pension:viewCount", currentIndex, currentIndex + PAGE_SIZE - 1);

        if (redisData == null || redisData.isEmpty()) {
            iterator = null;
            return;
        }

        // Step 2: Pension ID 리스트 추출
        List<Long> pensionIds = redisData.stream()
                .map(tuple -> Long.valueOf(tuple.getValue()))
                .toList();

        // Step 3: 배치로 Pension, Address, 평균 가격 조회
        Map<Long, Pension> pensionMap = pensionRepository.findByPensionIds(pensionIds)
                .stream()
                .collect(Collectors.toMap(Pension::getPensionId, p -> p));

        Map<Long, Address> addressMap = addressService.getAddressesForPensionsOrPlaces(pensionIds, "010");

        Map<Long, BigDecimal> avgPriceMap = roomService.findAveragePricesByPensionIds(pensionIds);

        // Step 4: Redis 데이터와 병합된 정보를 DTO로 변환
        List<RedisTopPensionDto> dtoList = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : redisData) {
            Long pensionId = Long.valueOf(tuple.getValue());
            Double score = tuple.getScore();

            Pension pension = pensionMap.get(pensionId);
            Address address = addressMap.get(pensionId);
            BigDecimal roomPriceAvg = avgPriceMap.getOrDefault(pensionId, BigDecimal.ZERO);

            if (pension != null) {
                RedisTopPensionDto dto = new RedisTopPensionDto(
                        pension.getPensionId(),
                        score,
                        globalRank++, // 전역 rank 사용
                        pension.getName(),
                        pension.getReviewCount(),
                        pension.getReviewAvg(),
                        pension.getLikeCount(),
                        address != null ? address.getProvince() : null,
                        address != null ? address.getCityDistrict() : null,
                        address != null ? address.getSubDistrict() : null,
                        roomPriceAvg,
                        pension.getPensionTags().stream()
                                .map(tag -> tag.getTag().getTagId())
                                .collect(Collectors.toList())
                );
                dtoList.add(dto); // DTO 추가
            }
        }
        iterator = dtoList.iterator();
        currentIndex += PAGE_SIZE;
    }
}
