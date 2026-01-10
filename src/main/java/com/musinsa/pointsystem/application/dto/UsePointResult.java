package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsePointResult {
    private final Long transactionId;
    private final Long memberId;
    private final Long usedAmount;
    private final Long totalBalance;
    private final String orderId;
}
