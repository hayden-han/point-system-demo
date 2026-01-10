package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.usecase.CancelEarnPointUseCase;
import com.musinsa.pointsystem.application.usecase.EarnPointUseCase;
import com.musinsa.pointsystem.presentation.dto.request.EarnPointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelEarnPointResponse;
import com.musinsa.pointsystem.presentation.dto.response.EarnPointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members/{memberId}/points/earn")
@RequiredArgsConstructor
public class PointEarnController {

    private final EarnPointUseCase earnPointUseCase;
    private final CancelEarnPointUseCase cancelEarnPointUseCase;

    @PostMapping
    public EarnPointResponse earn(
            @PathVariable Long memberId,
            @Valid @RequestBody EarnPointRequest request) {
        EarnPointCommand command = EarnPointCommand.builder()
                .memberId(memberId)
                .amount(request.getAmount())
                .earnType(request.getEarnType())
                .expirationDays(request.getExpirationDays())
                .build();

        EarnPointResult result = earnPointUseCase.execute(command);
        return EarnPointResponse.from(result);
    }

    @PostMapping("/{ledgerId}/cancel")
    public CancelEarnPointResponse cancelEarn(
            @PathVariable Long memberId,
            @PathVariable Long ledgerId) {
        CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                .memberId(memberId)
                .ledgerId(ledgerId)
                .build();

        CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);
        return CancelEarnPointResponse.from(result);
    }
}
