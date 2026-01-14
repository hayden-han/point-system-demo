package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 변동 이력 조회 결과
 * - 도메인 레이어의 순수 Value Object
 */
public record PointHistory(
        UUID entryId,
        UUID ledgerId,
        EntryType type,
        long amount,
        String orderId,
        LocalDateTime createdAt
) {
    public static PointHistory of(
            UUID entryId,
            UUID ledgerId,
            EntryType type,
            long amount,
            String orderId,
            LocalDateTime createdAt
    ) {
        return new PointHistory(entryId, ledgerId, type, amount, orderId, createdAt);
    }
}
