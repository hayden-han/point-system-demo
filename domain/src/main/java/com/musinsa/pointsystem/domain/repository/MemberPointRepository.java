package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * MemberPoint Repository 인터페이스
 *
 * <h3>설계 원칙:</h3>
 * <ul>
 *   <li>MemberPoint는 DB 테이블 없이 Ledger 기반으로 조립</li>
 *   <li>totalBalance는 조회 시점에 계산</li>
 *   <li>LedgerEntry 포함 조회로 변동 이력 추적</li>
 * </ul>
 *
 * <h3>메서드 네이밍 컨벤션:</h3>
 * <ul>
 *   <li>{@code findBy...} - 조회, 없으면 Optional.empty()</li>
 *   <li>{@code getBy...} - 조회, 없으면 예외 발생</li>
 *   <li>{@code getOrCreate...} - 조회, 없으면 생성</li>
 *   <li>{@code ...WithAllLedgers} - 모든 Ledger 포함</li>
 *   <li>{@code ...WithAvailableLedgersForUse} - 사용 가능한 Ledger만</li>
 *   <li>{@code ...AndEntries} - LedgerEntry 포함</li>
 * </ul>
 */
public interface MemberPointRepository {

    // =====================================================
    // LedgerEntry 포함 조회
    // =====================================================

    /**
     * 회원 포인트 조회 (모든 Ledger + Entry 포함)
     */
    Optional<MemberPoint> findByMemberIdWithAllLedgersAndEntries(UUID memberId);

    /**
     * 사용 가능한 Ledger + Entry 포함 조회
     * @param now 현재 시간 (만료 판단용)
     */
    Optional<MemberPoint> findByMemberIdWithAvailableLedgersAndEntries(UUID memberId, LocalDateTime now);

    /**
     * 특정 주문에서 사용된 Ledger + Entry 포함 조회
     * - 사용취소 시 사용
     */
    Optional<MemberPoint> findByMemberIdWithLedgersForOrder(UUID memberId, String orderId);

    /**
     * 특정 Ledger + Entry 포함 조회
     * - 적립취소 시 사용 (해당 Ledger만 로드하여 최적화)
     */
    Optional<MemberPoint> findByMemberIdWithSpecificLedger(UUID memberId, UUID ledgerId);

    // =====================================================
    // 저장
    // =====================================================

    /**
     * MemberPoint 저장 (Ledger + Entry 모두 저장)
     */
    MemberPoint saveWithEntries(MemberPoint memberPoint);

    // =====================================================
    // getOrCreate 메서드
    // =====================================================

    /**
     * 모든 Ledger + Entry 포함 조회, 없으면 생성
     */
    default MemberPoint getOrCreateWithAllLedgersAndEntries(UUID memberId) {
        return findByMemberIdWithAllLedgersAndEntries(memberId)
                .orElseGet(() -> MemberPoint.create(memberId));
    }

    // =====================================================
    // getBy 메서드 (없으면 예외)
    // =====================================================

    /**
     * 모든 Ledger + Entry 포함 조회, 없으면 예외 발생
     * @throws MemberPointNotFoundException 회원 포인트가 없는 경우
     */
    default MemberPoint getByMemberIdWithAllLedgersAndEntries(UUID memberId) {
        return findByMemberIdWithAllLedgersAndEntries(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }
}
