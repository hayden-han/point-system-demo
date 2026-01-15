package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EarnPointCommand(
        UUID memberId,
        Long amount,
        String earnType,
        Integer expirationDays
) {}
