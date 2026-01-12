package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "적립 취소 응답")
@Builder
public record CancelEarnPointResponse(

        @Schema(description = "취소된 적립건 ID")
        UUID ledgerId,

        @Schema(description = "취소 트랜잭션 ID")
        UUID transactionId,

        @Schema(description = "회원 ID")
        UUID memberId,

        @Schema(description = "취소된 금액", example = "1000")
        Long canceledAmount,

        @Schema(description = "취소 후 총 잔액", example = "4000")
        Long totalBalance
) {
    public static CancelEarnPointResponse from(CancelEarnPointResult result) {
        return CancelEarnPointResponse.builder()
                .ledgerId(result.ledgerId())
                .transactionId(result.transactionId())
                .memberId(result.memberId())
                .canceledAmount(result.canceledAmount())
                .totalBalance(result.totalBalance())
                .build();
    }
}
