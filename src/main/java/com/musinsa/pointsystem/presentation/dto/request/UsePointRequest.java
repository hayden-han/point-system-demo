package com.musinsa.pointsystem.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsePointRequest {

    @NotNull(message = "사용 금액은 필수입니다.")
    @Positive(message = "사용 금액은 양수여야 합니다.")
    private Long amount;

    @NotBlank(message = "주문번호는 필수입니다.")
    private String orderId;
}
