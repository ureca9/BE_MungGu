package com.meong9.backend.global.batch.place.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class TopPlaceBatchLauncher {

    private final JobLauncher jobLauncher;

    private final Job aggregateTopPlaceJob;

    @Scheduled(cron = "0 0 0 * * ?")
    public void launchTopPlaceJob() {
        log.info("Top Place Job 시작 - 시작 시간: {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 간결한 키
                    .toJobParameters();
            jobLauncher.run(aggregateTopPlaceJob, jobParameters);
        } catch (Exception e) {
            log.error("Top Place Job 실행 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Top Place Job 종료 - 종료 시간: {}, 소요 시간: {}ms",
                    LocalDateTime.now(), (endTime - startTime));
        }
    }
}