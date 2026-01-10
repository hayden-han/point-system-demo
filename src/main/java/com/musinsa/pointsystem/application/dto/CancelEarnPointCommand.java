package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelEarnPointCommand {
    private final Long memberId;
    private final Long ledgerId;
}
