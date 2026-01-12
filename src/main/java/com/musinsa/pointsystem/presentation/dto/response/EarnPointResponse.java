package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.EarnPointResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "포인트 적립 응답")
@Getter
@Builder
public class EarnPointResponse {

    @Schema(description = "적립건 ID")
    private final UUID ledgerId;

    @Schema(description = "트랜잭션 ID")
    private final UUID transactionId;

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "적립 금액", example = "1000")
    private final Long earnedAmount;

    @Schema(description = "적립 후 총 잔액", example = "5000")
    private final Long totalBalance;

    @Schema(description = "만료일시", example = "2025-01-01T00:00:00")
    private final LocalDateTime expiredAt;

    public static EarnPointResponse from(EarnPointResult result) {
        return EarnPointResponse.builder()
                .ledgerId(result.getLedgerId())
                .transactionId(result.getTransactionId())
                .memberId(result.getMemberId())
                .earnedAmount(result.getEarnedAmount())
                .totalBalance(result.getTotalBalance())
                .expiredAt(result.getExpiredAt())
                .build();
    }
}
