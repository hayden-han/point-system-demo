package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelEarnPointResponse {
    private final Long ledgerId;
    private final Long transactionId;
    private final Long memberId;
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
