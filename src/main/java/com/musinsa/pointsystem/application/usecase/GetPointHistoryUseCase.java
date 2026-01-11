package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPointHistoryUseCase {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public PageResult<PointTransaction> execute(UUID memberId, PageRequest pageRequest) {
        return pointTransactionRepository.findByMemberId(memberId, pageRequest);
    }
}
