package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.*;
import com.musinsa.pointsystem.application.usecase.*;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.presentation.dto.request.*;
import com.musinsa.pointsystem.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PointBalanceResponse> getBalance(@PathVariable Long memberId) {
        MemberPoint memberPoint = getPointBalanceUseCase.execute(memberId);
        return ResponseEntity.ok(PointBalanceResponse.from(memberPoint));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PointTransactionResponse>> getHistory(
            @PathVariable Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PointTransaction> transactions = getPointHistoryUseCase.execute(memberId, pageable);
        return ResponseEntity.ok(transactions.map(PointTransactionResponse::from));
    }

    @PostMapping("/earn")
    public ResponseEntity<EarnPointResponse> earn(
            @PathVariable Long memberId,
            @Valid @RequestBody EarnPointRequest request) {
        EarnPointCommand command = EarnPointCommand.builder()
                .memberId(memberId)
                .amount(request.getAmount())
                .earnType(request.getEarnType())
                .expirationDays(request.getExpirationDays())
                .build();

        EarnPointResult result = earnPointUseCase.execute(command);
        return ResponseEntity.ok(EarnPointResponse.from(result));
    }

    @PostMapping("/earn/{ledgerId}/cancel")
    public ResponseEntity<CancelEarnPointResponse> cancelEarn(
            @PathVariable Long memberId,
            @PathVariable Long ledgerId) {
        CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                .memberId(memberId)
                .ledgerId(ledgerId)
                .build();

        CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);
        return ResponseEntity.ok(CancelEarnPointResponse.from(result));
    }

    @PostMapping("/use")
    public ResponseEntity<UsePointResponse> use(
            @PathVariable Long memberId,
            @Valid @RequestBody UsePointRequest request) {
        UsePointCommand command = UsePointCommand.builder()
                .memberId(memberId)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .build();

        UsePointResult result = usePointUseCase.execute(command);
        return ResponseEntity.ok(UsePointResponse.from(result));
    }

    @PostMapping("/use/cancel")
    public ResponseEntity<CancelUsePointResponse> cancelUse(
            @PathVariable Long memberId,
            @Valid @RequestBody CancelUsePointRequest request) {
        CancelUsePointCommand command = CancelUsePointCommand.builder()
                .memberId(memberId)
                .transactionId(request.getTransactionId())
                .cancelAmount(request.getCancelAmount())
                .build();

        CancelUsePointResult result = cancelUsePointUseCase.execute(command);
        return ResponseEntity.ok(CancelUsePointResponse.from(result));
    }
}
