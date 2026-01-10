package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EarnPointResult {
    private final Long ledgerId;
    private final Long transactionId;
    private final Long memberId;
    private final Long earnedAmount;
    private final Long totalBalance;
    private final LocalDateTime expiredAt;
}
