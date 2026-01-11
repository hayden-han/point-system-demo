package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointTransactionFixture {

    public static PointTransaction createEarn(UUID id, UUID memberId, Long amount, UUID ledgerId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.EARN)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createEarnCancel(UUID id, UUID memberId, Long amount, UUID ledgerId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.EARN_CANCEL)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUse(UUID id, UUID memberId, Long amount, String orderId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.USE)
                .amount(amount)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUseCancel(UUID id, UUID memberId, Long amount,
                                                    String orderId, UUID relatedTransactionId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.USE_CANCEL)
                .amount(amount)
                .orderId(orderId)
                .relatedTransactionId(relatedTransactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
