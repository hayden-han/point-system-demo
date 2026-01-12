package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 도메인 이벤트 sealed interface
 * - 이벤트 소싱의 핵심 요소
 * - 모든 상태 변경은 이벤트로 표현
 * - sealed로 허용된 이벤트 타입만 존재
 */
public sealed interface PointEvent permits
        PointEarnedEvent,
        PointEarnCanceledEvent,
        PointUsedEvent,
        PointUseCanceledEvent {

    /**
     * 이벤트 고유 ID
     */
    UUID getEventId();

    /**
     * Aggregate ID (memberId)
     */
    UUID getAggregateId();

    /**
     * 이벤트 버전 (낙관적 동시성 제어)
     */
    long getVersion();

    /**
     * 이벤트 발생 시각
     */
    LocalDateTime getOccurredAt();

    /**
     * 이벤트 타입 문자열 (직렬화용)
     */
    String getEventType();
}
