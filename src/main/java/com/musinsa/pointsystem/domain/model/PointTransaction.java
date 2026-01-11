package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PointTransaction {
    private final UUID id;
    private final UUID memberId;
    private final TransactionType type;
    private final Long amount;
    private final String orderId;
    private final UUID relatedTransactionId;
    private final UUID ledgerId;
    private final LocalDateTime createdAt;

    @Builder
    public PointTransaction(UUID id, UUID memberId, TransactionType type, Long amount,
                            String orderId, UUID relatedTransactionId, UUID ledgerId,
                            LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.relatedTransactionId = relatedTransactionId;
        this.ledgerId = ledgerId;
        this.createdAt = createdAt;
    }

    public static PointTransaction createEarn(UUID memberId, Long amount, UUID ledgerId) {
        return PointTransaction.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .type(TransactionType.EARN)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createEarnCancel(UUID memberId, Long amount, UUID ledgerId) {
        return PointTransaction.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .type(TransactionType.EARN_CANCEL)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUse(UUID memberId, Long amount, String orderId) {
        return PointTransaction.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .type(TransactionType.USE)
                .amount(amount)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUseCancel(UUID memberId, Long amount, String orderId, UUID relatedTransactionId) {
        return PointTransaction.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .type(TransactionType.USE_CANCEL)
                .amount(amount)
                .orderId(orderId)
                .relatedTransactionId(relatedTransactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
