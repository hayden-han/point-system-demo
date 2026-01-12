package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelUsePointCommand(
        UUID memberId,
        UUID transactionId,
        Long cancelAmount
) {}
