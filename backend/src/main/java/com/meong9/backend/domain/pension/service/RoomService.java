package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.pension.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * 특정 pensionId 리스트에 대해 평균 객실 가격을 조회하고 Map 형태로 반환합니다.
     *
     * @param pensionIds 평균 가격을 조회할 Pension ID 리스트
     * @return pensionId를 Key로, 평균 객실 가격(BigDecimal)을 Value로 하는 Map
     */
    public Map<Long, BigDecimal> findAveragePricesByPensionIds(List<Long> pensionIds) {
        // RoomRepository에서 평균 객실 가격 데이터를 조회
        List<Object[]> results = roomRepository.findAveragePricesByPensionIds(pensionIds);

        // 결과를 Stream으로 변환하여 pensionId를 Key, 평균 가격을 Value로 Map 생성
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],          // pensionId
                        result -> BigDecimal.valueOf((Double) result[1]) // 평균 가격 변환
                ));
    }

}

