package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UsePointResult {
    private final UUID transactionId;
    private final UUID memberId;
    private final Long usedAmount;
    private final Long totalBalance;
    private final String orderId;
}
