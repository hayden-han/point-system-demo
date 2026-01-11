package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CancelEarnPointResponse {
    private final UUID ledgerId;
    private final UUID transactionId;
    private final UUID memberId;
    private final Long canceledAmount;
    private final Long totalBalance;

    public static CancelEarnPointResponse from(CancelEarnPointResult result) {
        return CancelEarnPointResponse.builder()
                .ledgerId(result.getLedgerId())
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .canceledAmount(result.getCanceledAmount())
                .totalBalance(result.getTotalBalance())
                .build();
    }
}
