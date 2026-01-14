package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelEarnPointResult(
        UUID ledgerId,
        UUID memberId,
        Long canceledAmount,
        Long totalBalance
) {}
