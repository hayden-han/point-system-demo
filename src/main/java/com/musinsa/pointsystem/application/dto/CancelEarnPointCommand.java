package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CancelEarnPointCommand {
    private final UUID memberId;
    private final UUID ledgerId;
}
