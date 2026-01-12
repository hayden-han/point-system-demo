package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립 취소 이벤트
 * - 적립건이 취소되었을 때 발행
 * - 전액 미사용 상태에서만 취소 가능
 */
public record PointEarnCanceledEvent(
        UUID eventId,
        UUID aggregateId,
        UUID ledgerId,
        long canceledAmount,
        long version,
        LocalDateTime occurredAt
) implements PointEvent {

    public static final String EVENT_TYPE = "POINT_EARN_CANCELED";

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UUID getAggregateId() {
        return aggregateId;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}
