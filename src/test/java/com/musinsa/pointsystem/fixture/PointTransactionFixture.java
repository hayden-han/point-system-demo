package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;

import java.time.LocalDateTime;

public class PointTransactionFixture {

    public static PointTransaction createEarn(Long id, Long memberId, Long amount, Long ledgerId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.EARN)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createEarnCancel(Long id, Long memberId, Long amount, Long ledgerId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.EARN_CANCEL)
                .amount(amount)
                .ledgerId(ledgerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUse(Long id, Long memberId, Long amount, String orderId) {
        return PointTransaction.builder()
                .id(id)
                .memberId(memberId)
                .type(TransactionType.USE)
                .amount(amount)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointTransaction createUseCancel(Long id, Long memberId, Long amount,
                                                    String orderId, Long relatedTransactionId) {
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
