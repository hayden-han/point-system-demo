package com.musinsa.pointsystem.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelUsePointRequest {

    @NotNull(message = "원본 트랜잭션 ID는 필수입니다.")
    private Long transactionId;

    @NotNull(message = "취소 금액은 필수입니다.")
    @Positive(message = "취소 금액은 양수여야 합니다.")
    private Long cancelAmount;
}
