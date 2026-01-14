package com.musinsa.pointsystem.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Schema(description = "포인트 적립 요청")
@Builder
public record EarnPointRequest(

        @Schema(description = "적립 금액 (1 ~ 100,000)", example = "1000", minimum = "1", maximum = "100000")
        @NotNull(message = "적립 금액은 필수입니다.")
        @Positive(message = "적립 금액은 양수여야 합니다.")
        Long amount,

        @Schema(description = "적립 유형", example = "SYSTEM", allowableValues = {"MANUAL", "SYSTEM"})
        @NotNull(message = "적립 타입은 필수입니다.")
        @Pattern(regexp = "^(MANUAL|SYSTEM)$", message = "적립 타입은 MANUAL 또는 SYSTEM이어야 합니다.")
        String earnType,

        @Schema(description = "만료일 (일 단위, 미입력 시 기본 365일)", example = "365", minimum = "1", maximum = "1824")
        Integer expirationDays
) {}
