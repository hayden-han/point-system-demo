package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.PointHistoryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "포인트 변동 이력")
@Builder
public record PointHistoryResponse(

        @Schema(description = "Entry ID")
        UUID entryId,

        @Schema(description = "적립건 ID")
        UUID ledgerId,

        @Schema(description = "변동 유형", example = "EARN", allowableValues = {"EARN", "EARN_CANCEL", "USE", "USE_CANCEL"})
        String type,

        @Schema(description = "금액 (양수: 적립/복구, 음수: 사용/취소)", example = "1000")
        Long amount,

        @Schema(description = "주문번호 (사용/사용취소 시)", example = "ORD-20240101-001")
        String orderId,

        @Schema(description = "변동 일시", example = "2024-01-01T12:00:00")
        LocalDateTime createdAt
) {
    public static PointHistoryResponse from(PointHistoryResult result) {
        return PointHistoryResponse.builder()
                .entryId(result.entryId())
                .ledgerId(result.ledgerId())
                .type(result.type())
                .amount(result.amount())
                .orderId(result.orderId())
                .createdAt(result.createdAt())
                .build();
    }
}
