package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 도메인 이벤트 기본 인터페이스
 */
public sealed interface PointEvent permits
        PointEarnedEvent,
        PointEarnCanceledEvent,
        PointUsedEvent,
        PointUseCanceledEvent {

    UUID memberId();
    long amount();
    LocalDateTime occurredAt();
}
