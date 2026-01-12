package com.musinsa.pointsystem.domain.event;

import com.musinsa.pointsystem.domain.model.EarnType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립 이벤트
 * - 회원에게 포인트가 적립되었을 때 발행
 * - 새로운 PointLedger 생성에 해당
 */
public record PointEarnedEvent(
        UUID eventId,
        UUID aggregateId,
        UUID ledgerId,
        long amount,
        EarnType earnType,
        LocalDateTime expiredAt,
        long version,
        LocalDateTime occurredAt
) implements PointEvent {

    public static final String EVENT_TYPE = "POINT_EARNED";

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
