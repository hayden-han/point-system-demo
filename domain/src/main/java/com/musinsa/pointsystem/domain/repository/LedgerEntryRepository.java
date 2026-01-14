package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.LedgerEntry;

import java.util.List;
import java.util.UUID;

/**
 * LedgerEntry Repository 인터페이스 (entries 기반)
 */
public interface LedgerEntryRepository {

    /**
     * Entry 저장
     */
    LedgerEntry save(LedgerEntry entry, UUID ledgerId);

    /**
     * 여러 Entry 저장
     */
    List<LedgerEntry> saveAll(List<LedgerEntry> entries, UUID ledgerId);

    /**
     * 적립건 ID로 Entry 목록 조회
     */
    List<LedgerEntry> findByLedgerId(UUID ledgerId);

    /**
     * 여러 적립건 ID로 Entry 목록 조회
     */
    List<LedgerEntry> findByLedgerIds(List<UUID> ledgerIds);

    /**
     * 주문 ID로 Entry 목록 조회
     */
    List<LedgerEntry> findByOrderId(String orderId);
}
