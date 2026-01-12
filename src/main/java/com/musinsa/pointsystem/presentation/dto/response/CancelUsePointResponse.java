package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "사용 취소 응답")
@Builder
public record CancelUsePointResponse(

        @Schema(description = "취소 트랜잭션 ID")
        UUID transactionId,

        @Schema(description = "회원 ID")
        UUID memberId,

        @Schema(description = "취소된 금액", example = "500")
        Long canceledAmount,

        @Schema(description = "취소 후 총 잔액", example = "5000")
        Long totalBalance,

        @Schema(description = "주문번호", example = "ORD-20240101-001")
        String orderId
) {
    public static CancelUsePointResponse from(CancelUsePointResult result) {
        return CancelUsePointResponse.builder()
                .transactionId(result.transactionId())
                .memberId(result.memberId())
                .canceledAmount(result.canceledAmount())
                .totalBalance(result.totalBalance())
                .orderId(result.orderId())
                .build();
    }
}
