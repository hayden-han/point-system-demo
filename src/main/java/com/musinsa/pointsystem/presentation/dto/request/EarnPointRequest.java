package com.musinsa.pointsystem.presentation.dto.request;

import com.musinsa.pointsystem.domain.model.EarnType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EarnPointRequest {

    @NotNull(message = "적립 금액은 필수입니다.")
    @Positive(message = "적립 금액은 양수여야 합니다.")
    private Long amount;

    @NotNull(message = "적립 타입은 필수입니다.")
    private EarnType earnType;

    private Integer expirationDays;
}
