package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PointUsageDetailJpaRepository extends JpaRepository<PointUsageDetailEntity, UUID> {

    List<PointUsageDetailEntity> findByTransactionId(UUID transactionId);

    @Query("SELECT pud FROM PointUsageDetailEntity pud " +
           "JOIN PointLedgerEntity pl ON pud.ledgerId = pl.id " +
           "WHERE pud.transactionId = :transactionId " +
           "AND pud.usedAmount > pud.canceledAmount " +
           "ORDER BY pl.expiredAt DESC")
    List<PointUsageDetailEntity> findCancelableByTransactionId(@Param("transactionId") UUID transactionId);
}
