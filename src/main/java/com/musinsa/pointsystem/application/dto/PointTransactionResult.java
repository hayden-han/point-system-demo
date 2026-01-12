package com.musinsa.pointsystem.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 거래 이력 조회 결과 DTO
 * - Presentation 레이어에서 Domain 모델에 직접 의존하지 않도록 함
 */
@Getter
@Builder
public class PointTransactionResult {
    private final UUID transactionId;
    private final UUID memberId;
    private final String type;  // TransactionType의 문자열 표현
    private final Long amount;
    private final String orderId;
    private final UUID relatedTransactionId;
    private final UUID ledgerId;
    private final LocalDateTime createdAt;
}
