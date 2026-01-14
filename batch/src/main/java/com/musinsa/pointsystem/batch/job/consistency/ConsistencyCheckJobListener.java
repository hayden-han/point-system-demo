package com.musinsa.pointsystem.batch.job.consistency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 정합성 검증 Job 리스너
 * - Job 시작/종료 로깅
 * - 실행 시간 측정
 */
@Slf4j
public class ConsistencyCheckJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("========================================");
        log.info("Starting Consistency Check Job");
        log.info("Job Instance ID: {}", jobExecution.getJobInstance().getInstanceId());
        log.info("Job Execution ID: {}", jobExecution.getId());
        log.info("Start Time: {}", LocalDateTime.now());
        log.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(
                jobExecution.getStartTime(),
                jobExecution.getEndTime() != null ? jobExecution.getEndTime() : LocalDateTime.now()
        );

        log.info("========================================");
        log.info("Consistency Check Job Completed");
        log.info("Status: {}", jobExecution.getStatus());
        log.info("Duration: {} seconds", duration.getSeconds());
        log.info("Exit Status: {}", jobExecution.getExitStatus());

        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            log.info("No failures occurred");
        } else {
            log.error("Failures occurred:");
            jobExecution.getAllFailureExceptions().forEach(e ->
                    log.error("  - {}", e.getMessage())
            );
        }
        log.info("========================================");
    }
}
