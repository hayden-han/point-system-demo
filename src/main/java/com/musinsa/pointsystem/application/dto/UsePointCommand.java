package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsePointCommand {
    private final Long memberId;
    private final Long amount;
    private final String orderId;
}
