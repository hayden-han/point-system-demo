package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointUsageDetailJpaRepository extends JpaRepository<PointUsageDetailEntity, Long> {

    List<PointUsageDetailEntity> findByTransactionId(Long transactionId);

    @Query("SELECT pud FROM PointUsageDetailEntity pud " +
           "JOIN PointLedgerEntity pl ON pud.ledgerId = pl.id " +
           "WHERE pud.transactionId = :transactionId " +
           "AND pud.usedAmount > pud.canceledAmount " +
           "ORDER BY pl.expiredAt DESC")
    List<PointUsageDetailEntity> findCancelableByTransactionId(@Param("transactionId") Long transactionId);
}
