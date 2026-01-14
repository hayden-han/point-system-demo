package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UsePointResult(
        UUID memberId,
        Long usedAmount,
        Long totalBalance,
        String orderId
) {}
