package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.UsePointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "포인트 사용 응답")
@Getter
@Builder
public class UsePointResponse {

    @Schema(description = "사용 트랜잭션 ID")
    private final UUID transactionId;

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "사용 금액", example = "500")
    private final Long usedAmount;

    @Schema(description = "사용 후 총 잔액", example = "4500")
    private final Long totalBalance;

    @Schema(description = "주문번호", example = "ORD-20240101-001")
    private final String orderId;

    public static UsePointResponse from(UsePointResult result) {
        return UsePointResponse.builder()
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .usedAmount(result.getUsedAmount())
                .totalBalance(result.getTotalBalance())
                .orderId(result.getOrderId())
                .build();
    }
}
