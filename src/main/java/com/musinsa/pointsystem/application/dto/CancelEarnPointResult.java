package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelEarnPointResult(
        UUID ledgerId,
        UUID transactionId,
        UUID memberId,
        Long canceledAmount,
        Long totalBalance
) {}
