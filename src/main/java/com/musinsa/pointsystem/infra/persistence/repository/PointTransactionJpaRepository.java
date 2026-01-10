package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransactionEntity, Long> {
    Page<PointTransactionEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
