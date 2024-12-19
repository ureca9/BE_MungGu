package com.meong9.backend.global.batch.place.listener;

import com.meong9.backend.global.utils.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopPlaceJobListener implements JobExecutionListener {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Job이 성공적으로 종료되면 Redis의 조회수 데이터를 삭제합니다.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            log.warn("Job 실패로 Redis 데이터 삭제 생략");
            return;
        }

        // Redis 데이터 삭제
        clearAllPlaceViewCounts();
        log.info("Redis의 시설 조회수 데이터가 삭제되었습니다.");
    }

    /**
     * 모든 펜션의 조회수 데이터를 삭제합니다.
     */
    private void clearAllPlaceViewCounts() {
        // 카테고리 목록 가져오기
        List<String> categoryList = CategoryMapper.getAllCategoryNames();

        for (String category : categoryList) {
            // Redis 정렬된 세트 키 생성
            String sortedSetKey = "place:category:" + category;

            // 키 존재 여부 확인
            Boolean hasKey = redisTemplate.hasKey(sortedSetKey);

            if (hasKey != null && hasKey) {
                // 키 삭제
                redisTemplate.delete(sortedSetKey);
                log.info("카테고리 {} 의 Redis 데이터가 삭제되었습니다.", category);
            } else {
                // 키가 존재하지 않을 경우 경고 로그
                log.warn("카테고리 {} 의 Redis 데이터가 존재하지 않습니다.", category);
            }
        }
    }
}
