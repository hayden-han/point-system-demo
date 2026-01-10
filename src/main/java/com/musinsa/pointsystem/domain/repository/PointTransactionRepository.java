package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction pointTransaction);
    Optional<PointTransaction> findById(Long id);
    Page<PointTransaction> findByMemberId(Long memberId, Pageable pageable);
}
