package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 포인트 스냅샷 JPA Repository
 */
public interface PointSnapshotJpaRepository extends JpaRepository<PointSnapshotEntity, UUID> {

    /**
     * Aggregate의 스냅샷 조회
     */
    Optional<PointSnapshotEntity> findByAggregateId(UUID aggregateId);
}
