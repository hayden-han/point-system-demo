package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 사용 이벤트
 */
public record PointUsedEvent(
        UUID memberId,
        long amount,
        String orderId,
        int usedLedgerCount,
        LocalDateTime occurredAt
) implements PointEvent {

    public static PointUsedEvent of(
            UUID memberId,
            long amount,
            String orderId,
            int usedLedgerCount,
            LocalDateTime now
    ) {
        return new PointUsedEvent(memberId, amount, orderId, usedLedgerCount, now);
    }
}
