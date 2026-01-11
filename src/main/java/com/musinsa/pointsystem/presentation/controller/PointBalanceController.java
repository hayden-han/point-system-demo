package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.usecase.GetPointBalanceUseCase;
import com.musinsa.pointsystem.application.usecase.GetPointHistoryUseCase;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.presentation.dto.response.PageResponse;
import com.musinsa.pointsystem.presentation.dto.response.PointBalanceResponse;
import com.musinsa.pointsystem.presentation.dto.response.PointTransactionResponse;
import lombok.RequiredArgsConstructor;
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
    public PageResponse<PointTransactionResponse> getHistory(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PointTransaction> transactions = getPointHistoryUseCase.execute(memberId, pageRequest);
        return PageResponse.from(transactions, PointTransactionResponse::from);
    }
}
