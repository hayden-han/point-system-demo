package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PointBalanceResult;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPointBalanceUseCase {

    private final MemberPointRepository memberPointRepository;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public PointBalanceResult execute(UUID memberId) {
        LocalDateTime now = timeProvider.now();

        // v2: 모든 Ledger 포함 조회 후 잔액 계산
        MemberPoint memberPoint = memberPointRepository.getOrCreateWithAllLedgersAndEntries(memberId);

        return PointBalanceResult.builder()
                .memberId(memberPoint.memberId())
                .totalBalance(memberPoint.getTotalBalance(now).getValue())
                .build();
    }
}
