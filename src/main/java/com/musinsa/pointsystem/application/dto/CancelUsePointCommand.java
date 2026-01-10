package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelUsePointCommand {
    private final Long memberId;
    private final Long transactionId;
    private final Long cancelAmount;
}
