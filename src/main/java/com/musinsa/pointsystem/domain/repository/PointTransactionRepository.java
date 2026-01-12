package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction pointTransaction);

    Optional<PointTransaction> findById(UUID id);

    PageResult<PointTransaction> findByMemberId(UUID memberId, PageRequest pageRequest);

    /**
     * 트랜잭션 조회, 없으면 예외 발생
     * @throws PointTransactionNotFoundException 트랜잭션이 없는 경우
     */
    default PointTransaction getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new PointTransactionNotFoundException(id));
    }
}
