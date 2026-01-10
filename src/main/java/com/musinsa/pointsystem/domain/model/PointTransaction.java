package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointTransaction {
    private final Long id;
    private final Long memberId;
    private final TransactionType type;
    private final Long amount;
    private final String orderId;
    private final Long relatedTransactionId;
    private final Long ledgerId;
    private final LocalDateTime createdAt;

    @Builder
    public PointTransaction(Long id, Long memberId, TransactionType type, Long amount,
                            String orderId, Long relatedTransactionId, Long ledgerId,
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

    public static PointTransaction createEarn(Long memberId, Long amount, Long ledgerId) {
        return PointTransaction.builder()
                .memberId(memberId)
                .type(TransactionType.EARN)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createEarnCancel(Long memberId, Long amount, Long ledgerId) {
        return PointTransaction.builder()
                .memberId(memberId)
                .type(TransactionType.EARN_CANCEL)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUse(Long memberId, Long amount, String orderId) {
        return PointTransaction.builder()
                .memberId(memberId)
                .type(TransactionType.USE)
                .amount(amount)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUseCancel(Long memberId, Long amount, String orderId, Long relatedTransactionId) {
        return PointTransaction.builder()
                .memberId(memberId)
                .type(TransactionType.USE_CANCEL)
                .amount(amount)
                .orderId(orderId)
                .relatedTransactionId(relatedTransactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
