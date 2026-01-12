package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.EarnPointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "포인트 적립 응답")
@Builder
public record EarnPointResponse(

        @Schema(description = "적립건 ID")
        UUID ledgerId,

        @Schema(description = "트랜잭션 ID")
        UUID transactionId,

        @Schema(description = "회원 ID")
        UUID memberId,

        @Schema(description = "적립 금액", example = "1000")
        Long earnedAmount,

        @Schema(description = "적립 후 총 잔액", example = "5000")
        Long totalBalance,

        @Schema(description = "만료일시", example = "2025-01-01T00:00:00")
        LocalDateTime expiredAt
) {
    public static EarnPointResponse from(EarnPointResult result) {
        return EarnPointResponse.builder()
                .ledgerId(result.ledgerId())
                .transactionId(result.transactionId())
                .memberId(result.memberId())
                .earnedAmount(result.earnedAmount())
                .totalBalance(result.totalBalance())
                .expiredAt(result.expiredAt())
                .build();
    }
}
