package com.musinsa.pointsystem.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Schema(description = "포인트 사용 취소 요청")
@Builder
public record CancelUsePointRequest(

        @Schema(description = "취소할 원본 주문 ID", example = "ORDER-2024-001")
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,

        @Schema(description = "취소 금액 (전체 또는 부분 취소)", example = "500", minimum = "1")
        @NotNull(message = "취소 금액은 필수입니다.")
        @Positive(message = "취소 금액은 양수여야 합니다.")
        Long cancelAmount
) {}
