package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.model.MemberPointSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 포인트 이벤트 저장소 인터페이스
 * - 이벤트 소싱의 핵심 인프라
 * - 낙관적 동시성 제어 지원
 * - 스냅샷 기반 성능 최적화
 */
public interface PointEventStore {

    /**
     * 이벤트 저장 (낙관적 동시성 제어)
     *
     * @param aggregateId     Aggregate ID (memberId)
     * @param events          저장할 이벤트 목록
     * @param expectedVersion 예상 버전 (동시성 체크)
     * @throws ConcurrencyConflictException 버전 충돌 시
     */
    void append(UUID aggregateId, List<PointEvent> events, long expectedVersion);

    /**
     * Aggregate의 모든 이벤트 조회
     *
     * @param aggregateId Aggregate ID (memberId)
     * @return 버전 순서로 정렬된 이벤트 목록
     */
    List<PointEvent> getEvents(UUID aggregateId);

    /**
     * 특정 버전 이후의 이벤트 조회
     *
     * @param aggregateId Aggregate ID (memberId)
     * @param fromVersion 시작 버전 (exclusive)
     * @return fromVersion 이후 이벤트 목록
     */
    List<PointEvent> getEvents(UUID aggregateId, long fromVersion);

    /**
     * 최신 스냅샷 조회
     *
     * @param aggregateId Aggregate ID (memberId)
     * @return 스냅샷 (없으면 empty)
     */
    Optional<MemberPointSnapshot> getLatestSnapshot(UUID aggregateId);

    /**
     * 스냅샷 저장
     *
     * @param snapshot 저장할 스냅샷
     */
    void saveSnapshot(MemberPointSnapshot snapshot);

    /**
     * Aggregate의 현재 버전 조회
     *
     * @param aggregateId Aggregate ID (memberId)
     * @return 현재 버전 (이벤트 없으면 0)
     */
    long getCurrentVersion(UUID aggregateId);

    /**
     * 동시성 충돌 예외
     */
    class ConcurrencyConflictException extends RuntimeException {
        private final UUID aggregateId;
        private final long expectedVersion;
        private final long actualVersion;

        public ConcurrencyConflictException(UUID aggregateId, long expectedVersion, long actualVersion) {
            super(String.format(
                    "Concurrency conflict for aggregate %s: expected version %d, actual version %d",
                    aggregateId, expectedVersion, actualVersion
            ));
            this.aggregateId = aggregateId;
            this.expectedVersion = expectedVersion;
            this.actualVersion = actualVersion;
        }

        public UUID getAggregateId() {
            return aggregateId;
        }

        public long getExpectedVersion() {
            return expectedVersion;
        }

        public long getActualVersion() {
            return actualVersion;
        }
    }
}
