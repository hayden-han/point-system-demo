package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 사용취소 이벤트
 */
public record PointUseCanceledEvent(
        UUID memberId,
        long amount,
        String orderId,
        int newLedgerCount,
        LocalDateTime occurredAt
) implements PointEvent {

    public static PointUseCanceledEvent of(
            UUID memberId,
            long amount,
            String orderId,
            int newLedgerCount,
            LocalDateTime now
    ) {
        return new PointUseCanceledEvent(memberId, amount, orderId, newLedgerCount, now);
    }
}
