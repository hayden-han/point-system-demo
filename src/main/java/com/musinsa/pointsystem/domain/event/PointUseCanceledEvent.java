package com.musinsa.pointsystem.domain.event;

import com.musinsa.pointsystem.domain.model.EarnType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 취소 이벤트
 * - 사용된 포인트가 취소되었을 때 발행
 * - 원본 적립건 복구 또는 신규 적립건 생성
 */
public record PointUseCanceledEvent(
        UUID eventId,
        UUID aggregateId,
        UUID originalTransactionId,
        long canceledAmount,
        String orderId,
        List<RestoredLedger> restoredLedgers,
        List<NewLedger> newLedgers,
        long version,
        LocalDateTime occurredAt
) implements PointEvent {

    public static final String EVENT_TYPE = "POINT_USE_CANCELED";

    /**
     * 복구된 적립건 정보
     * - 만료되지 않은 원본 적립건에서 복구
     */
    public record RestoredLedger(
            UUID ledgerId,
            long restoredAmount
    ) {}

    /**
     * 신규 생성 적립건 정보
     * - 원본 적립건이 만료된 경우 신규 생성
     */
    public record NewLedger(
            UUID ledgerId,
            long amount,
            EarnType earnType,
            LocalDateTime expiredAt
    ) {}

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
