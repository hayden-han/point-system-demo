package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EarnPointResult {
    private final UUID ledgerId;
    private final UUID transactionId;
    private final UUID memberId;
    private final Long earnedAmount;
    private final Long totalBalance;
    private final LocalDateTime expiredAt;
}
