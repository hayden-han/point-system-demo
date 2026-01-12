package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.PointBalanceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "포인트 잔액 응답")
@Getter
@Builder
public class PointBalanceResponse {

    @Schema(description = "회원 ID")
    private final UUID memberId;

    @Schema(description = "총 잔액", example = "5000")
    private final Long totalBalance;

    public static PointBalanceResponse from(PointBalanceResult result) {
        return PointBalanceResponse.builder()
                .memberId(result.getMemberId())
                .totalBalance(result.getTotalBalance())
                .build();
    }
}
