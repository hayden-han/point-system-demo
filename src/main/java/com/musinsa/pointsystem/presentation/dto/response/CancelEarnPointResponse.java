package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "적립 취소 응답")
@Getter
@Builder
public class CancelEarnPointResponse {

    @Schema(description = "취소된 적립건 ID")
    private final UUID ledgerId;

    @Schema(description = "취소 트랜잭션 ID")
    private final UUID transactionId;

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "취소된 금액", example = "1000")
    private final Long canceledAmount;

    @Schema(description = "취소 후 총 잔액", example = "4000")
    private final Long totalBalance;

    public static CancelEarnPointResponse from(CancelEarnPointResult result) {
        return CancelEarnPointResponse.builder()
                .ledgerId(result.getLedgerId())
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .canceledAmount(result.getCanceledAmount())
                .totalBalance(result.getTotalBalance())
                .build();
    }
}
