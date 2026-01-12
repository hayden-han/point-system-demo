package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CancelEarnPointCommand(
        UUID memberId,
        UUID ledgerId
) {}
