package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.UsePointResult;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UsePointResponse {
    private final UUID transactionId;
    private final UUID memberId;
    private final Long usedAmount;
    private final Long totalBalance;
    private final String orderId;

    public static UsePointResponse from(UsePointResult result) {
        return UsePointResponse.builder()
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .usedAmount(result.getUsedAmount())
                .totalBalance(result.getTotalBalance())
                .orderId(result.getOrderId())
                .build();
    }
}
