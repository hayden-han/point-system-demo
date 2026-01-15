package com.musinsa.pointsystem.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립 이벤트
 */
public record PointEarnedEvent(
        UUID memberId,
        UUID ledgerId,
        long amount,
        String earnType,
        LocalDateTime expiredAt,
        LocalDateTime occurredAt
) implements PointEvent {

    public static PointEarnedEvent of(
            UUID memberId,
            UUID ledgerId,
            long amount,
            String earnType,
            LocalDateTime expiredAt,
            LocalDateTime now
    ) {
        return new PointEarnedEvent(memberId, ledgerId, amount, earnType, expiredAt, now);
    }
}
