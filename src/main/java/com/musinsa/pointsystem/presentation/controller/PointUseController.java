package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.usecase.CancelUsePointUseCase;
import com.musinsa.pointsystem.application.usecase.UsePointUseCase;
import com.musinsa.pointsystem.presentation.dto.request.CancelUsePointRequest;
import com.musinsa.pointsystem.presentation.dto.request.UsePointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelUsePointResponse;
import com.musinsa.pointsystem.presentation.dto.response.UsePointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members/{memberId}/points/use")
@RequiredArgsConstructor
public class PointUseController {

    private final UsePointUseCase usePointUseCase;
    private final CancelUsePointUseCase cancelUsePointUseCase;

    @PostMapping
    public UsePointResponse use(
            @PathVariable Long memberId,
            @Valid @RequestBody UsePointRequest request) {
        UsePointCommand command = UsePointCommand.builder()
                .memberId(memberId)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .build();

        UsePointResult result = usePointUseCase.execute(command);
        return UsePointResponse.from(result);
    }

    @PostMapping("/cancel")
    public CancelUsePointResponse cancelUse(
            @PathVariable Long memberId,
            @Valid @RequestBody CancelUsePointRequest request) {
        CancelUsePointCommand command = CancelUsePointCommand.builder()
                .memberId(memberId)
                .transactionId(request.getTransactionId())
                .cancelAmount(request.getCancelAmount())
                .build();

        CancelUsePointResult result = cancelUsePointUseCase.execute(command);
        return CancelUsePointResponse.from(result);
    }
}
