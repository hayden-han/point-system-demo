package com.musinsa.pointsystem.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Schema(description = "포인트 사용 요청")
@Builder
public record UsePointRequest(

        @Schema(description = "사용 금액", example = "500", minimum = "1")
        @NotNull(message = "사용 금액은 필수입니다.")
        @Positive(message = "사용 금액은 양수여야 합니다.")
        Long amount,

        @Schema(description = "주문번호", example = "ORD-20240101-001")
        @NotBlank(message = "주문번호는 필수입니다.")
        String orderId
) {}
