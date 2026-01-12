package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record EarnPointResult(
        UUID ledgerId,
        UUID transactionId,
        UUID memberId,
        Long earnedAmount,
        Long totalBalance,
        LocalDateTime expiredAt
) {}
