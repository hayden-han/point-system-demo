package com.musinsa.pointsystem.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "포인트 사용 취소 요청")
@Builder
public record CancelUsePointRequest(

        @Schema(description = "취소할 원본 사용 트랜잭션 ID", example = "01938f9a-1234-7abc-def0-123456789abc")
        @NotNull(message = "원본 트랜잭션 ID는 필수입니다.")
        UUID transactionId,

        @Schema(description = "취소 금액 (전체 또는 부분 취소)", example = "500", minimum = "1")
        @NotNull(message = "취소 금액은 필수입니다.")
        @Positive(message = "취소 금액은 양수여야 합니다.")
        Long cancelAmount
) {}
