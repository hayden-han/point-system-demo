package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelUsePointResult {
    private final Long transactionId;
    private final Long memberId;
    private final Long canceledAmount;
    private final Long totalBalance;
    private final String orderId;
}
