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

    /**
     * ID로 조회, 없으면 PointTransactionNotFoundException 발생
     */
    default PointTransaction getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new PointTransactionNotFoundException(id));
    }

    PageResult<PointTransaction> findByMemberId(UUID memberId, PageRequest pageRequest);
}
