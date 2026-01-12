package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EarnPointCommand {
    private final UUID memberId;
    private final Long amount;
    private final String earnType;  // Domain EarnType이 아닌 문자열로 변경
    private final Integer expirationDays;
}
