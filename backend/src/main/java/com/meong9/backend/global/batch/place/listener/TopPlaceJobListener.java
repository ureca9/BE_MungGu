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
        List<String> categoryList = CategoryMapper.getAllCategoryNames();

        for (String category : categoryList) {
            String sortedSetKey = "place:category:" + category; // Redis Sorted Set 키 생성
            redisTemplate.delete(sortedSetKey);
        }
    }
}
