package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PointBalanceResult;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPointBalanceUseCase {

    private final MemberPointRepository memberPointRepository;

    @Transactional(readOnly = true)
    public PointBalanceResult execute(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.getOrCreate(memberId);
        return PointBalanceResult.builder()
                .memberId(memberPoint.getMemberId())
                .totalBalance(memberPoint.getTotalBalance().getValue())
                .build();
    }
}
