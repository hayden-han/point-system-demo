package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 적립건 변동 이력 (Value Object)
 * - 감사 로그 역할 통합
 * - Append-only (수정 불가)
 * - amount: 양수(적립/복구), 음수(사용/취소)
 */
public record LedgerEntry(
        UUID id,
        EntryType type,
        long amount,
        String orderId,
        LocalDateTime createdAt
) {
    public LedgerEntry {
        if (id == null) {
            throw new IllegalArgumentException("id는 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type은 필수입니다.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt은 필수입니다.");
        }
        validateAmountSign(type, amount);
    }

    private static void validateAmountSign(EntryType type, long amount) {
        switch (type) {
            case EARN, USE_CANCEL -> {
                if (amount <= 0) {
                    throw new IllegalArgumentException(type + " Entry의 amount는 양수여야 합니다: " + amount);
                }
            }
            case EARN_CANCEL, USE -> {
                if (amount >= 0) {
                    throw new IllegalArgumentException(type + " Entry의 amount는 음수여야 합니다: " + amount);
                }
            }
        }
    }

    /**
     * EARN Entry 생성
     */
    public static LedgerEntry createEarn(UUID id, long amount, LocalDateTime createdAt) {
        return new LedgerEntry(id, EntryType.EARN, amount, null, createdAt);
    }

    /**
     * EARN_CANCEL Entry 생성
     */
    public static LedgerEntry createEarnCancel(UUID id, long amount, LocalDateTime createdAt) {
        return new LedgerEntry(id, EntryType.EARN_CANCEL, -Math.abs(amount), null, createdAt);
    }

    /**
     * USE Entry 생성
     */
    public static LedgerEntry createUse(UUID id, long amount, String orderId, LocalDateTime createdAt) {
        return new LedgerEntry(id, EntryType.USE, -Math.abs(amount), orderId, createdAt);
    }

    /**
     * USE_CANCEL Entry 생성
     */
    public static LedgerEntry createUseCancel(UUID id, long amount, String orderId, LocalDateTime createdAt) {
        return new LedgerEntry(id, EntryType.USE_CANCEL, Math.abs(amount), orderId, createdAt);
    }

    /**
     * 금액의 절대값
     */
    public long absoluteAmount() {
        return Math.abs(amount);
    }

    /**
     * 양수 여부
     */
    public boolean isPositive() {
        return amount > 0;
    }

    /**
     * 음수 여부
     */
    public boolean isNegative() {
        return amount < 0;
    }
}
