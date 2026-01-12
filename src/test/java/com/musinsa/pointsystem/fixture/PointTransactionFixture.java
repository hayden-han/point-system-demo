package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointTransactionFixture {

    public static PointTransaction createEarn(UUID id, UUID memberId, Long amount, UUID ledgerId) {
        return new PointTransaction(
                id,
                memberId,
                TransactionType.EARN,
                PointAmount.of(amount),
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createEarnCancel(UUID id, UUID memberId, Long amount, UUID ledgerId) {
        return new PointTransaction(
                id,
                memberId,
                TransactionType.EARN_CANCEL,
                PointAmount.of(amount),
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createUse(UUID id, UUID memberId, Long amount, String orderId) {
        return new PointTransaction(
                id,
                memberId,
                TransactionType.USE,
                PointAmount.of(amount),
                OrderId.of(orderId),
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static PointTransaction createUseCancel(UUID id, UUID memberId, Long amount,
                                                    String orderId, UUID relatedTransactionId) {
        return new PointTransaction(
                id,
                memberId,
                TransactionType.USE_CANCEL,
                PointAmount.of(amount),
                OrderId.of(orderId),
                relatedTransactionId,
                null,
                LocalDateTime.now()
        );
    }
}
