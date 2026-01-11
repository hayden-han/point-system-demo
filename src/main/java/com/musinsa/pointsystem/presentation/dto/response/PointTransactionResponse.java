package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PointTransactionResponse {
    private final UUID transactionId;
    private final UUID memberId;
    private final TransactionType type;
    private final Long amount;
    private final String orderId;
    private final UUID relatedTransactionId;
    private final UUID ledgerId;
    private final LocalDateTime createdAt;

    public static PointTransactionResponse from(PointTransaction transaction) {
        return PointTransactionResponse.builder()
                .transactionId(transaction.getId())
                .memberId(transaction.getMemberId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .orderId(transaction.getOrderId())
                .relatedTransactionId(transaction.getRelatedTransactionId())
                .ledgerId(transaction.getLedgerId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
