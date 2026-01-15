package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 적립건 변동 이력 (데이터 구조)
 * - Append-only (수정 불가)
 * - amount: 양수(적립/복구), 음수(사용/취소)
 */
public record LedgerEntry(
        UUID id,
        UUID ledgerId,
        EntryType type,
        long amount,
        String orderId,
        LocalDateTime createdAt
) {
    public static LedgerEntry createEarn(UUID id, UUID ledgerId, long amount, LocalDateTime createdAt) {
        return new LedgerEntry(id, ledgerId, EntryType.EARN, amount, null, createdAt);
    }

    public static LedgerEntry createEarnCancel(UUID id, UUID ledgerId, long amount, LocalDateTime createdAt) {
        return new LedgerEntry(id, ledgerId, EntryType.EARN_CANCEL, -Math.abs(amount), null, createdAt);
    }

    public static LedgerEntry createUse(UUID id, UUID ledgerId, long amount, String orderId, LocalDateTime createdAt) {
        return new LedgerEntry(id, ledgerId, EntryType.USE, -Math.abs(amount), orderId, createdAt);
    }

    public static LedgerEntry createUseCancel(UUID id, UUID ledgerId, long amount, String orderId, LocalDateTime createdAt) {
        return new LedgerEntry(id, ledgerId, EntryType.USE_CANCEL, Math.abs(amount), orderId, createdAt);
    }

    public long absoluteAmount() {
        return Math.abs(amount);
    }
}
