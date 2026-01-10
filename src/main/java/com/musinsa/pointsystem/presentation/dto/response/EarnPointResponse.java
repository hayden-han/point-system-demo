package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.EarnPointResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EarnPointResponse {
    private final Long ledgerId;
    private final Long transactionId;
    private final Long memberId;
    private final Long earnedAmount;
    private final Long totalBalance;
    private final LocalDateTime expiredAt;

    public static EarnPointResponse from(EarnPointResult result) {
        return EarnPointResponse.builder()
                .ledgerId(result.getLedgerId())
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .earnedAmount(result.getEarnedAmount())
                .totalBalance(result.getTotalBalance())
                .expiredAt(result.getExpiredAt())
                .build();
    }
}
