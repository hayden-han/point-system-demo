package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointTransactionResponse {
    private final Long transactionId;
    private final Long memberId;
    private final TransactionType type;
    private final Long amount;
    private final String orderId;
    private final Long relatedTransactionId;
    private final Long ledgerId;
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
