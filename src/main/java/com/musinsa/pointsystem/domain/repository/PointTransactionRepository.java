package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction pointTransaction);
    Optional<PointTransaction> findById(UUID id);
    Page<PointTransaction> findByMemberId(UUID memberId, Pageable pageable);
}
