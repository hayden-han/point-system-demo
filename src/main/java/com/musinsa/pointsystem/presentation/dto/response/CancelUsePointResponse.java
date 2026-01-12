package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "사용 취소 응답")
@Getter
@Builder
public class CancelUsePointResponse {

    @Schema(description = "취소 트랜잭션 ID")
    private final UUID transactionId;

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "취소된 금액", example = "500")
    private final Long canceledAmount;

    @Schema(description = "취소 후 총 잔액", example = "5000")
    private final Long totalBalance;

    @Schema(description = "주문번호", example = "ORD-20240101-001")
    private final String orderId;

    public static CancelUsePointResponse from(CancelUsePointResult result) {
        return CancelUsePointResponse.builder()
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .canceledAmount(result.getCanceledAmount())
                .totalBalance(result.getTotalBalance())
                .orderId(result.getOrderId())
                .build();
    }
}
