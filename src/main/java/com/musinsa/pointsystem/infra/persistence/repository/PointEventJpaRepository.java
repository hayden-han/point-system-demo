package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 포인트 이벤트 JPA Repository
 */
public interface PointEventJpaRepository extends JpaRepository<PointEventEntity, UUID> {

    /**
     * Aggregate의 모든 이벤트 조회 (버전 순서)
     */
    List<PointEventEntity> findByAggregateIdOrderByVersionAsc(UUID aggregateId);

    /**
     * 특정 버전 이후 이벤트 조회 (버전 순서)
     */
    List<PointEventEntity> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(
            UUID aggregateId, Long fromVersion);

    /**
     * Aggregate의 현재 최대 버전 조회
     */
    @Query("SELECT MAX(e.version) FROM PointEventEntity e WHERE e.aggregateId = :aggregateId")
    Optional<Long> findMaxVersionByAggregateId(@Param("aggregateId") UUID aggregateId);

    /**
     * Aggregate의 이벤트 개수 조회
     */
    long countByAggregateId(UUID aggregateId);
}
