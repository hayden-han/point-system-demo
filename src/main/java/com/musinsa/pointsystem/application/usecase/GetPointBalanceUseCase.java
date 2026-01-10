package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPointBalanceUseCase {

    private final MemberPointRepository memberPointRepository;

    @Transactional(readOnly = true)
    public MemberPoint execute(Long memberId) {
        return memberPointRepository.getOrCreate(memberId);
    }
}
