package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.PointTransactionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "포인트 거래 이력")
@Builder
public record PointTransactionResponse(

        @Schema(description = "트랜잭션 ID")
        UUID transactionId,

        @Schema(description = "회원 ID")
        UUID memberId,

        @Schema(description = "거래 유형", example = "EARN", allowableValues = {"EARN", "EARN_CANCEL", "USE", "USE_CANCEL"})
        String type,

        @Schema(description = "금액", example = "1000")
        Long amount,

        @Schema(description = "주문번호 (사용/사용취소 시)", example = "ORD-20240101-001")
        String orderId,

        @Schema(description = "관련 트랜잭션 ID (취소 시 원본 트랜잭션)")
        UUID relatedTransactionId,

        @Schema(description = "적립건 ID (적립/적립취소 시)")
        UUID ledgerId,

        @Schema(description = "거래 일시", example = "2024-01-01T12:00:00")
        LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransactionResult result) {
        return PointTransactionResponse.builder()
                .transactionId(result.transactionId())
                .memberId(result.memberId())
                .type(result.type())
                .amount(result.amount())
                .orderId(result.orderId())
                .relatedTransactionId(result.relatedTransactionId())
                .ledgerId(result.ledgerId())
                .createdAt(result.createdAt())
                .build();
    }
}
