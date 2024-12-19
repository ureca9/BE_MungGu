package com.meong9.backend.global.batch.pension.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component("pensionJobExecutionContextCleaner")
@Slf4j
public class PensionJobExecutionContextCleaner implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job이 성공적으로 완료되었습니다. ExecutionContext 데이터를 삭제합니다.");
            cleanExecutionContext(jobExecution); // 컨텍스트 정리
        } else {
            log.info("Job이 중단되었거나 실패했습니다. ExecutionContext 데이터는 유지됩니다.");
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job이 시작되었습니다.");
    }

    private void cleanExecutionContext(JobExecution jobExecution) {
        // JobExecutionContext 가져오기
        ExecutionContext jobContext = jobExecution.getExecutionContext();

        for (String key : KEYS_TO_REMOVE) {
            if (jobContext.containsKey(key)) {
                jobContext.remove(key); // 해당 키 삭제
                log.info("JobExecutionContext에서 Key '{}' 데이터를 삭제했습니다.", key);
            } else {
                log.warn("JobExecutionContext에 Key '{}'가 존재하지 않습니다.", key);
            }
        }
        log.debug("삭제 후 ExecutionContext 상태: {}", jobContext);
    }


    private static final String[] KEYS_TO_REMOVE = {"topPensionIds", "topFeatureIds"};

}
