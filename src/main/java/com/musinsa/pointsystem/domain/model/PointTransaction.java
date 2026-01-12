package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;

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
        LocalDateTime createdAt
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Static Factory Methods
    public static PointTransaction createEarn(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                UuidGenerator.generate(),
                memberId,
                TransactionType.EARN,
                amount,
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createEarnCancel(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                UuidGenerator.generate(),
                memberId,
                TransactionType.EARN_CANCEL,
                amount,
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createUse(UUID memberId, PointAmount amount, OrderId orderId) {
        return new PointTransaction(
                UuidGenerator.generate(),
                memberId,
                TransactionType.USE,
                amount,
                orderId,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createUseCancel(UUID memberId, PointAmount amount, OrderId orderId, UUID relatedTransactionId) {
        return new PointTransaction(
                UuidGenerator.generate(),
                memberId,
                TransactionType.USE_CANCEL,
                amount,
                orderId,
                relatedTransactionId,
                null,
                LocalDateTime.now()
        );
    }
}
