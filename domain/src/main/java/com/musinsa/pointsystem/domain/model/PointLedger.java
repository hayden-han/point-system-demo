package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립건 (데이터 구조)
 * - 비즈니스 로직은 PointRules에 위치
 */
public record PointLedger(
        UUID id,
        UUID memberId,
        long earnedAmount,
        long availableAmount,
        EarnType earnType,
        UUID sourceLedgerId,
        LocalDateTime expiredAt,
        boolean canceled,
        LocalDateTime earnedAt
) {
    public static PointLedger create(
            UUID id,
            UUID memberId,
            long amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID sourceLedgerId,
            LocalDateTime now
    ) {
        return new PointLedger(
                id, memberId, amount, amount, earnType, sourceLedgerId,
                expiredAt, false, now
        );
    }

    public PointLedger withAvailableAmount(long newAvailableAmount) {
        return new PointLedger(
                id, memberId, earnedAmount, newAvailableAmount,
                earnType, sourceLedgerId, expiredAt, canceled, earnedAt
        );
    }

    public PointLedger withCanceled() {
        return new PointLedger(
                id, memberId, earnedAmount, 0,
                earnType, sourceLedgerId, expiredAt, true, earnedAt
        );
    }

    public boolean isManual() {
        return earnType == EarnType.MANUAL;
    }
}
