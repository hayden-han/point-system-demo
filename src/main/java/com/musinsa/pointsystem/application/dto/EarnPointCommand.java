package com.musinsa.pointsystem.application.dto;

import com.musinsa.pointsystem.domain.model.EarnType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EarnPointCommand {
    private final UUID memberId;
    private final Long amount;
    private final EarnType earnType;
    private final Integer expirationDays;
}
