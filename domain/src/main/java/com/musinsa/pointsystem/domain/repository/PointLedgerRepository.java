package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PointLedger Repository 인터페이스
 * - N+1 방지를 위해 Entry 함께 조회하는 메서드 제공
 */
public interface PointLedgerRepository {

    // =====================================================
    // 단일 조회
    // =====================================================

    Optional<PointLedger> findById(UUID id);

    // =====================================================
    // 목록 조회 (N+1 방지 - Entry 별도 조회 필요)
    // =====================================================

    /**
     * 회원의 사용 가능한 Ledger 조회 (만료/취소 제외)
     * - 우선순위: 수기 적립 우선, 만료일 빠른 순
     */
    List<PointLedger> findAvailableByMemberId(UUID memberId, LocalDateTime now);

    /**
     * 회원의 모든 Ledger 조회
     */
    List<PointLedger> findAllByMemberId(UUID memberId);

    /**
     * 특정 Ledger ID 목록으로 조회
     */
    List<PointLedger> findAllByIds(List<UUID> ids);

    // =====================================================
    // 저장
    // =====================================================

    PointLedger save(PointLedger ledger);

    List<PointLedger> saveAll(List<PointLedger> ledgers);
}
