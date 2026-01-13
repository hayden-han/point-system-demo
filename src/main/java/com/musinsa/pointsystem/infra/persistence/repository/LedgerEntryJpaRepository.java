package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    /**
     * 적립건 ID로 Entry 목록 조회
     */
    List<LedgerEntryEntity> findByLedgerIdOrderByCreatedAtAsc(UUID ledgerId);

    /**
     * 여러 적립건 ID로 Entry 목록 조회
     */
    @Query("SELECT e FROM LedgerEntryEntity e WHERE e.ledgerId IN :ledgerIds ORDER BY e.createdAt ASC")
    List<LedgerEntryEntity> findByLedgerIdInOrderByCreatedAtAsc(@Param("ledgerIds") List<UUID> ledgerIds);

    /**
     * 주문 ID로 Entry 목록 조회
     */
    List<LedgerEntryEntity> findByOrderIdOrderByCreatedAtAsc(String orderId);

    /**
     * 주문 ID로 관련된 Ledger ID 목록 조회
     */
    @Query("SELECT DISTINCT e.ledgerId FROM LedgerEntryEntity e WHERE e.orderId = :orderId")
    List<UUID> findDistinctLedgerIdsByOrderId(@Param("orderId") String orderId);
}
