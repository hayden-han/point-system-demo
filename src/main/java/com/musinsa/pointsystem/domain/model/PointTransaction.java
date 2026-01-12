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
}
