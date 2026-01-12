package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 거래 이력 (Append-only 감사 로그)
 * - 불변 record
 * - 상태 변경 없음 (생성 후 불변)
 */
public record PointTransaction(
        UUID id,
        UUID memberId,
        TransactionType type,
        PointAmount amount,
        OrderId orderId,
        UUID relatedTransactionId,
        UUID ledgerId,
        LocalDateTime transactedAt
) {
    // 기존 코드 호환성을 위한 getter 메서드들
    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public TransactionType getType() {
        return type;
    }

    public PointAmount getAmount() {
        return amount;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UUID getRelatedTransactionId() {
        return relatedTransactionId;
    }

    public UUID getLedgerId() {
        return ledgerId;
    }

    public LocalDateTime getTransactedAt() {
        return transactedAt;
    }
}
