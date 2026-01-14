package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UsePointCommand(
        UUID memberId,
        Long amount,
        String orderId
) {}
