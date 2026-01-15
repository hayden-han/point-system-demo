package com.musinsa.pointsystem.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 변동 이력 조회 결과 DTO
 * - LedgerEntry 기반
 */
@Builder
public record PointHistoryResult(
        UUID entryId,
        UUID ledgerId,
        String type,
        Long amount,
        String orderId,
        LocalDateTime createdAt
) {}
