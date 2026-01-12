package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 이벤트
 * - 회원이 포인트를 사용했을 때 발행
 * - 여러 적립건에서 차감될 수 있으므로 UsageDetail 목록 포함
 */
public record PointUsedEvent(
        UUID eventId,
        UUID aggregateId,
        UUID transactionId,
        long amount,
        String orderId,
        List<UsageDetail> usageDetails,
        long version,
        LocalDateTime occurredAt
) implements PointEvent {

    public static final String EVENT_TYPE = "POINT_USED";

    /**
     * 적립건별 사용 상세
     */
    public record UsageDetail(
            UUID ledgerId,
            long usedAmount
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
