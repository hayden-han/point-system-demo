package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.PointTransactionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "포인트 거래 이력")
@Getter
@Builder
public class PointTransactionResponse {

    @Schema(description = "트랜잭션 ID")
    private final UUID transactionId;

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "거래 유형", example = "EARN", allowableValues = {"EARN", "EARN_CANCEL", "USE", "USE_CANCEL"})
    private final String type;

    @Schema(description = "금액", example = "1000")
    private final Long amount;

    @Schema(description = "주문번호 (사용/사용취소 시)", example = "ORD-20240101-001")
    private final String orderId;

    @Schema(description = "관련 트랜잭션 ID (취소 시 원본 트랜잭션)")
    private final UUID relatedTransactionId;

    @Schema(description = "적립건 ID (적립/적립취소 시)")
    private final UUID ledgerId;

    @Schema(description = "거래 일시", example = "2024-01-01T12:00:00")
    private final LocalDateTime createdAt;

    public static PointTransactionResponse from(PointTransactionResult result) {
        return PointTransactionResponse.builder()
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .type(result.getType())
                .amount(result.getAmount())
                .orderId(result.getOrderId())
                .relatedTransactionId(result.getRelatedTransactionId())
                .ledgerId(result.getLedgerId())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
