package com.musinsa.pointsystem.batch.job.consistency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * 정합성 검증 Step 리스너
 * - Step 실행 통계 로깅
 */
@Slf4j
public class ConsistencyCheckStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step completed: {}", stepExecution.getStepName());
        log.info("  - Read count: {}", stepExecution.getReadCount());
        log.info("  - Write count: {}", stepExecution.getWriteCount());
        log.info("  - Skip count (read): {}", stepExecution.getReadSkipCount());
        log.info("  - Skip count (write): {}", stepExecution.getWriteSkipCount());
        log.info("  - Skip count (process): {}", stepExecution.getProcessSkipCount());
        log.info("  - Commit count: {}", stepExecution.getCommitCount());
        log.info("  - Rollback count: {}", stepExecution.getRollbackCount());

        return stepExecution.getExitStatus();
    }
}
