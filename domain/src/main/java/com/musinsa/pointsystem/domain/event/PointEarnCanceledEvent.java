package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립취소 이벤트
 */
public record PointEarnCanceledEvent(
        UUID memberId,
        UUID ledgerId,
        long amount,
        LocalDateTime occurredAt
) implements PointEvent {

    public static PointEarnCanceledEvent of(
            UUID memberId,
            UUID ledgerId,
            long amount,
            LocalDateTime now
    ) {
        return new PointEarnCanceledEvent(memberId, ledgerId, amount, now);
    }
}
