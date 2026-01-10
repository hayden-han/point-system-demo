package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointLedgerJpaRepository extends JpaRepository<PointLedgerEntity, Long> {

    @Query("SELECT pl FROM PointLedgerEntity pl " +
           "WHERE pl.memberId = :memberId " +
           "AND pl.availableAmount > 0 " +
           "AND pl.expiredAt > CURRENT_TIMESTAMP " +
           "AND pl.isCanceled = false " +
           "ORDER BY CASE pl.earnType WHEN 'MANUAL' THEN 0 ELSE 1 END, pl.expiredAt ASC")
    List<PointLedgerEntity> findAvailableByMemberId(@Param("memberId") Long memberId);
}
