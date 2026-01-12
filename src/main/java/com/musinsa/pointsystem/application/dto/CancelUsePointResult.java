package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelUsePointResult(
        UUID transactionId,
        UUID memberId,
        Long canceledAmount,
        Long totalBalance,
        String orderId
) {}
