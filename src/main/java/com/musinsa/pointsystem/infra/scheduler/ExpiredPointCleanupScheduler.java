package com.musinsa.pointsystem.infra.scheduler;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.infra.persistence.repository.PointLedgerJpaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 만료된 포인트 정리 스케줄러
 * - 매일 새벽 3시에 실행
 * - 청크 단위 처리로 락 경합 방지
 * - 만료된 적립건의 잔액을 0으로 설정
 */
@Component
@Slf4j
public class ExpiredPointCleanupScheduler {

    private static final int CHUNK_SIZE = 1000;  // 한 번에 처리할 건수
    private static final int MAX_ITERATIONS = 100;  // 무한 루프 방지

    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final TimeProvider timeProvider;
    private final Counter cleanupCounter;
    private final Counter errorCounter;
    private final Timer cleanupTimer;

    public ExpiredPointCleanupScheduler(PointLedgerJpaRepository pointLedgerJpaRepository,
                                         TimeProvider timeProvider,
                                         MeterRegistry meterRegistry) {
        this.pointLedgerJpaRepository = pointLedgerJpaRepository;
        this.timeProvider = timeProvider;
        this.cleanupCounter = Counter.builder("point.expired.cleanup")
                .description("Number of expired point ledgers cleaned up")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("point.expired.cleanup.error")
                .description("Number of errors during expired point cleanup")
                .register(meterRegistry);
        this.cleanupTimer = Timer.builder("point.expired.cleanup.duration")
                .description("Duration of expired point cleanup")
                .register(meterRegistry);
    }

    /**
     * 매일 새벽 3시에 만료된 포인트 정리
     * - 청크 단위로 처리하여 락 경합 방지
     * - 각 청크는 별도 트랜잭션으로 처리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredPoints() {
        cleanupTimer.record(() -> {
            log.info("만료 포인트 정리 시작");
            LocalDateTime now = timeProvider.now();

            int totalUpdatedCount = 0;
            int iteration = 0;

            try {
                // 전체 만료 건수 조회
                long totalExpiredCount = pointLedgerJpaRepository.countExpiredLedgers(now);
                log.info("만료 대상 적립건 수: {}", totalExpiredCount);

                // 청크 단위로 처리
                while (iteration < MAX_ITERATIONS) {
                    int updatedCount = processChunk(now);
                    if (updatedCount == 0) {
                        break;  // 더 이상 처리할 데이터 없음
                    }
                    totalUpdatedCount += updatedCount;
                    iteration++;

                    // 진행 상황 로깅 (10번마다)
                    if (iteration % 10 == 0) {
                        log.info("만료 포인트 정리 진행 중. 현재까지 처리 건수: {}", totalUpdatedCount);
                    }
                }

                cleanupCounter.increment(totalUpdatedCount);
                log.info("만료 포인트 정리 완료. 총 처리 건수: {}, 반복 횟수: {}", totalUpdatedCount, iteration);

            } catch (Exception e) {
                errorCounter.increment();
                log.error("만료 포인트 정리 중 오류 발생. 현재까지 처리 건수: {}", totalUpdatedCount, e);
                // 예외를 던지지 않고 로깅만 수행 (스케줄러가 중단되지 않도록)
            }
        });
    }

    /**
     * 청크 단위 처리 (독립 트랜잭션)
     * - 각 청크가 실패해도 이전 청크의 결과는 유지됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processChunk(LocalDateTime now) {
        return pointLedgerJpaRepository.markExpiredLedgersAsZeroBalanceWithLimit(now, CHUNK_SIZE);
    }
}
