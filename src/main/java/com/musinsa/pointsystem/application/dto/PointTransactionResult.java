package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 거래 이력 조회 결과 DTO
 * - Presentation 레이어에서 Domain 모델에 직접 의존하지 않도록 함
 */
@Builder
public record PointTransactionResult(
        UUID transactionId,
        UUID memberId,
        String type,
        Long amount,
        String orderId,
        UUID relatedTransactionId,
        UUID ledgerId,
        LocalDateTime createdAt
) {}
