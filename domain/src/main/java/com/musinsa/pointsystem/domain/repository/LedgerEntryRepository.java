package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.EntryType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;

import java.util.List;
import java.util.UUID;

/**
 * LedgerEntry Repository 인터페이스
 * - Append-only (저장만, 수정 없음)
 */
public interface LedgerEntryRepository {

    /**
     * Entry 저장
     */
    LedgerEntry save(LedgerEntry entry);

    /**
     * 여러 Entry 저장
     */
    List<LedgerEntry> saveAll(List<LedgerEntry> entries);

    /**
     * Ledger ID로 Entry 조회
     */
    List<LedgerEntry> findByLedgerId(UUID ledgerId);

    /**
     * 여러 Ledger ID로 Entry 조회 (N+1 방지)
     */
    List<LedgerEntry> findByLedgerIds(List<UUID> ledgerIds);

    /**
     * 주문 ID로 Entry 조회
     */
    List<LedgerEntry> findByOrderId(String orderId);

    /**
     * 주문 ID로 관련 Ledger ID 목록 조회
     */
    List<UUID> findLedgerIdsByOrderId(String orderId);

    /**
     * 주문 ID와 Entry Type으로 조회
     */
    List<LedgerEntry> findByOrderIdAndType(String orderId, EntryType type);
}
