package com.meong9.backend.global.batch.pension.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class StepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step '{}' 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Step '{}' 실패. 처리된 아이템: {}, 건너뛴 아이템: {}, 실패 원인: {}",
                    stepExecution.getStepName(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount(),
                    stepExecution.getFailureExceptions());
            return ExitStatus.FAILED;
        } else {
            log.info("Step '{}' 완료. 처리된 아이템: {}, 건너뛴 아이템: {}",
                    stepExecution.getStepName(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount());
            return ExitStatus.COMPLETED;
        }
    }

}