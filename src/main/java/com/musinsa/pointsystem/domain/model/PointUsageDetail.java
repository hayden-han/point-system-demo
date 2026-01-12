package com.musinsa.pointsystem.domain.model;

import java.util.UUID;

/**
 * 포인트 사용 상세 (어떤 적립건에서 얼마나 사용했는지 추적)
 * - 불변 record + wither 패턴
 * - 상태 변경 시 새 객체 반환
 */
public record PointUsageDetail(
        UUID id,
        UUID transactionId,
        UUID ledgerId,
        PointAmount usedAmount,
        PointAmount canceledAmount
) {
    // Compact constructor - 기본값 설정
    public PointUsageDetail {
        if (canceledAmount == null) {
            canceledAmount = PointAmount.ZERO;
        }
    }

    // 비즈니스 메서드
    public PointAmount getCancelableAmount() {
        return usedAmount.subtract(canceledAmount);
    }

    public boolean isCancelable() {
        return getCancelableAmount().isPositive();
    }

    /**
     * 취소 처리 (불변 - 새 객체 반환)
     * @param amount 취소할 금액
     * @return [새 PointUsageDetail, 실제 취소된 금액] 튜플
     */
    public CancelResult cancel(PointAmount amount) {
        PointAmount cancelAmount = amount.min(getCancelableAmount());
        PointUsageDetail updated = new PointUsageDetail(
                id,
                transactionId,
                ledgerId,
                usedAmount,
                canceledAmount.add(cancelAmount)
        );
        return new CancelResult(updated, cancelAmount);
    }

    /**
     * 취소 결과 (새 객체 + 취소된 금액)
     */
    public record CancelResult(PointUsageDetail usageDetail, PointAmount canceledAmount) {}
}
