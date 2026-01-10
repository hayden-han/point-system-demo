package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.*;
import com.musinsa.pointsystem.application.usecase.*;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.presentation.dto.request.CancelUsePointRequest;
import com.musinsa.pointsystem.presentation.dto.request.EarnPointRequest;
import com.musinsa.pointsystem.presentation.dto.request.UsePointRequest;
import com.musinsa.pointsystem.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members/{memberId}/points")
@RequiredArgsConstructor
public class PointController {

    private final EarnPointUseCase earnPointUseCase;
    private final CancelEarnPointUseCase cancelEarnPointUseCase;
    private final UsePointUseCase usePointUseCase;
    private final CancelUsePointUseCase cancelUsePointUseCase;
    private final GetPointBalanceUseCase getPointBalanceUseCase;
    private final GetPointHistoryUseCase getPointHistoryUseCase;

    @GetMapping
    public PointBalanceResponse getBalance(@PathVariable Long memberId) {
        MemberPoint memberPoint = getPointBalanceUseCase.execute(memberId);
        return PointBalanceResponse.from(memberPoint);
    }

    @GetMapping("/history")
    public Page<PointTransactionResponse> getHistory(
            @PathVariable Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PointTransaction> transactions = getPointHistoryUseCase.execute(memberId, pageable);
        return transactions.map(PointTransactionResponse::from);
    }

    @PostMapping("/earn")
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

    @PostMapping("/earn/{ledgerId}/cancel")
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

    @PostMapping("/use")
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

    @PostMapping("/use/cancel")
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
