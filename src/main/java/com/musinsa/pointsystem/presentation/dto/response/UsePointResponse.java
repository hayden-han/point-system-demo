package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.UsePointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "포인트 사용 응답")
@Builder
public record UsePointResponse(

        @Schema(description = "사용 트랜잭션 ID")
        UUID transactionId,

        @Schema(description = "회원 ID")
        UUID memberId,

        @Schema(description = "사용 금액", example = "500")
        Long usedAmount,

        @Schema(description = "사용 후 총 잔액", example = "4500")
        Long totalBalance,

        @Schema(description = "주문번호", example = "ORD-20240101-001")
        String orderId
) {
    public static UsePointResponse from(UsePointResult result) {
        return UsePointResponse.builder()
                .transactionId(result.transactionId())
                .memberId(result.memberId())
                .usedAmount(result.usedAmount())
                .totalBalance(result.totalBalance())
                .orderId(result.orderId())
                .build();
    }
}
