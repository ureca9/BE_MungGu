package com.meong9.backend.global.batch.pension.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component("pensionJobExecutionContextCleaner")
@Slf4j
public class PensionJobExecutionContextCleaner implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Job 성공 상태 확인
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job이 성공적으로 완료되었습니다. ExecutionContext 데이터를 삭제합니다.");

            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                String[] keysToRemove = {"topPensionIds", "topFeatureIds"};

                for (String key : keysToRemove) {
                    if (stepExecution.getExecutionContext().containsKey(key)) {
                        stepExecution.getExecutionContext().remove(key);
                        log.info("StepExecutionContext에서 {}를 삭제했습니다.", key);
                    }
                }
            }
        } else {
            log.info("Job이 중단되었거나 실패했습니다. ExecutionContext 데이터는 유지됩니다.");
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Job 성공 상태 확인
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job이 성공적으로 완료되었습니다. ExecutionContext 데이터를 삭제합니다.");

            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                String[] keysToRemove = {"topPensionIds", "topFeatureIds"};

                for (String key : keysToRemove) {
                    if (stepExecution.getExecutionContext().containsKey(key)) {
                        stepExecution.getExecutionContext().remove(key);
                        log.info("StepExecutionContext에서 {}를 삭제했습니다.", key);
                    }
                }
            }
        } else {
            log.info("Job이 중단되었거나 실패했습니다. ExecutionContext 데이터는 유지됩니다.");
        }
    }
}
