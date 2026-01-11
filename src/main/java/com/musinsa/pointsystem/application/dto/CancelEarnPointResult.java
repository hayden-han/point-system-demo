package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CancelEarnPointResult {
    private final UUID ledgerId;
    private final UUID transactionId;
    private final UUID memberId;
    private final Long canceledAmount;
    private final Long totalBalance;
}
