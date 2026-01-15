package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelUsePointCommand(
        UUID memberId,
        String orderId,
        Long cancelAmount
) {}
