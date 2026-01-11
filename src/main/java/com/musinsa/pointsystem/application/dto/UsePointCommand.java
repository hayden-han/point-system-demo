package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UsePointCommand {
    private final UUID memberId;
    private final Long amount;
    private final String orderId;
}
