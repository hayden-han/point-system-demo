package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CancelUsePointResponse {
    private final UUID transactionId;
    private final UUID memberId;
    private final Long canceledAmount;
    private final Long totalBalance;
    private final String orderId;

    public static CancelUsePointResponse from(CancelUsePointResult result) {
        return CancelUsePointResponse.builder()
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .canceledAmount(result.getCanceledAmount())
                .totalBalance(result.getTotalBalance())
                .orderId(result.getOrderId())
                .build();
    }
}
