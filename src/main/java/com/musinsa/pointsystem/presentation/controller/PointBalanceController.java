package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.usecase.GetPointBalanceUseCase;
import com.musinsa.pointsystem.application.usecase.GetPointHistoryUseCase;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.presentation.dto.response.PointBalanceResponse;
import com.musinsa.pointsystem.presentation.dto.response.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members/{memberId}/points")
@RequiredArgsConstructor
public class PointBalanceController {

    private final GetPointBalanceUseCase getPointBalanceUseCase;
    private final GetPointHistoryUseCase getPointHistoryUseCase;

    @GetMapping
    public PointBalanceResponse getBalance(@PathVariable UUID memberId) {
        MemberPoint memberPoint = getPointBalanceUseCase.execute(memberId);
        return PointBalanceResponse.from(memberPoint);
    }

    @GetMapping("/history")
    public Page<PointTransactionResponse> getHistory(
            @PathVariable UUID memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PointTransaction> transactions = getPointHistoryUseCase.execute(memberId, pageable);
        return transactions.map(PointTransactionResponse::from);
    }
}
