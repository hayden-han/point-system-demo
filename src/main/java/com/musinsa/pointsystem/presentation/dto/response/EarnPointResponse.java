package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.EarnPointResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EarnPointResponse {
    private final UUID ledgerId;
    private final UUID transactionId;
    private final UUID memberId;
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
