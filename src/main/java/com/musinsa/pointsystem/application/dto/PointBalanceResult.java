package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * 포인트 잔액 조회 결과 DTO
 * - Presentation 레이어에서 Domain 모델에 직접 의존하지 않도록 함
 */
@Builder
public record PointBalanceResult(
        UUID memberId,
        Long totalBalance
) {}
