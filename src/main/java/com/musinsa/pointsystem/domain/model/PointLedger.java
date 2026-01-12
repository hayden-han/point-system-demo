package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립건
 * - 불변 record + wither 패턴
 * - 상태 변경 시 새 객체 반환
 */
public record PointLedger(
        UUID id,
        UUID memberId,
        PointAmount earnedAmount,
        PointAmount availableAmount,
        PointAmount usedAmount,
        EarnType earnType,
        UUID sourceTransactionId,
        LocalDateTime expiredAt,
        boolean canceled,
        LocalDateTime earnedAt
) {
    // Compact constructor - 기본값 설정
    public PointLedger {
        if (availableAmount == null) {
            availableAmount = PointAmount.ZERO;
        }
        if (usedAmount == null) {
            usedAmount = PointAmount.ZERO;
        }
    }

    // 기존 코드 호환성을 위한 getter 메서드들
    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public PointAmount getEarnedAmount() {
        return earnedAmount;
    }

    public PointAmount getAvailableAmount() {
        return availableAmount;
    }

    public PointAmount getUsedAmount() {
        return usedAmount;
    }

    public EarnType getEarnType() {
        return earnType;
    }

    public UUID getSourceTransactionId() {
        return sourceTransactionId;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    // 비즈니스 메서드
    public boolean canCancel() {
        return !canceled && earnedAmount.equals(availableAmount);
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isAvailable() {
        return !canceled && !isExpired() && availableAmount.isPositive();
    }

    public boolean isManual() {
        return earnType == EarnType.MANUAL;
    }

    /**
     * 적립 취소 수행 (불변 - 새 객체 반환)
     * - 이미 취소된 경우: PointLedgerAlreadyCanceledException
     * - 일부/전체 사용된 경우: PointLedgerAlreadyUsedException
     */
    public PointLedger cancel() {
        if (canceled) {
            throw new PointLedgerAlreadyCanceledException(id);
        }
        if (!earnedAmount.equals(availableAmount)) {
            throw new PointLedgerAlreadyUsedException(id);
        }
        return new PointLedger(
                id, memberId, earnedAmount, PointAmount.ZERO, usedAmount,
                earnType, sourceTransactionId, expiredAt, true, earnedAt
        );
    }

    /**
     * 사용 처리 (불변 - 새 객체 + 사용된 금액 반환)
     */
    public UseResult use(PointAmount amount) {
        PointAmount useAmount = amount.min(availableAmount);
        PointLedger updated = new PointLedger(
                id, memberId, earnedAmount,
                availableAmount.subtract(useAmount),
                usedAmount.add(useAmount),
                earnType, sourceTransactionId, expiredAt, canceled, earnedAt
        );
        return new UseResult(updated, useAmount);
    }

    /**
     * 사용 결과 (새 객체 + 사용된 금액)
     */
    public record UseResult(PointLedger ledger, PointAmount usedAmount) {}

    /**
     * 복구 처리 (불변 - 새 객체 반환)
     */
    public PointLedger restore(PointAmount amount) {
        if (amount.isGreaterThan(usedAmount)) {
            throw new IllegalStateException("복구할 금액이 사용된 금액보다 클 수 없습니다.");
        }
        return new PointLedger(
                id, memberId, earnedAmount,
                availableAmount.add(amount),
                usedAmount.subtract(amount),
                earnType, sourceTransactionId, expiredAt, canceled, earnedAt
        );
    }

}
