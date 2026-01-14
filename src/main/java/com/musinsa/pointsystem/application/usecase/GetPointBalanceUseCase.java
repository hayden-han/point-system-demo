package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PointBalanceResult;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 포인트 잔액 조회 UseCase
 *
 * <p>최적화: Aggregate 로드 없이 DB에서 직접 SUM 쿼리로 조회.
 * 조회 전용 PointQueryRepository를 사용하여 성능 최적화.
 */
@Service
@RequiredArgsConstructor
public class GetPointBalanceUseCase {

    private final PointQueryRepository pointQueryRepository;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public PointBalanceResult execute(UUID memberId) {
        // 최적화: Aggregate 로드 없이 DB에서 직접 계산
        PointAmount totalBalance = pointQueryRepository.getTotalBalance(
                memberId,
                timeProvider.now()
        );

        return PointBalanceResult.builder()
                .memberId(memberId)
                .totalBalance(totalBalance.getValue())
                .build();
    }
}
