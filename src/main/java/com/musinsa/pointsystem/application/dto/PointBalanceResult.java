package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 포인트 잔액 조회 결과 DTO
 * - Presentation 레이어에서 Domain 모델에 직접 의존하지 않도록 함
 */
@Getter
@Builder
public class PointBalanceResult {
    private final UUID memberId;
    private final Long totalBalance;
}
