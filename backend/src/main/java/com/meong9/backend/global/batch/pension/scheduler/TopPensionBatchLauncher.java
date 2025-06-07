package com.meong9.backend.global.batch.pension.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class TopPensionBatchLauncher {

    private final JobLauncher jobLauncher;
    @Qualifier("aggregateTopPensionJob")
    private final Job aggregateTopPensionJob;

    @Retryable(
            value = { JobExecutionException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Scheduled(cron = "0 0 0 * * ?")
    public void launchTopPensionJob() {
        long startTime = System.nanoTime();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobId", UUID.randomUUID().toString())
                    .addString("executionDate", LocalDate.now().toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            log.info("Top Pension Job 시작 - 배치ID: {}, 시작 시간: {}",
                    jobParameters.getString("jobId"), LocalDateTime.now());
            jobLauncher.run(aggregateTopPensionJob, jobParameters);
        } catch (JobExecutionException e) {
            log.error("Top_Pension 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Top_Pension 예상치 못한 오류 발생: {}", e.getMessage(), e);
        } finally {
            long endTime = System.nanoTime();
            log.info("Top Pension Job 종료 - 종료 시간: {}, 소요 시간: {}ms",
                    LocalDateTime.now(), TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
        }
    }
}