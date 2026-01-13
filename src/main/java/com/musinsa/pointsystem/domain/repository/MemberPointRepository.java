package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * MemberPoint Repository 인터페이스
 *
 * <h3>v2 변경사항:</h3>
 * <ul>
 *   <li>member_point 테이블 제거 예정 → Ledger 기반 조회</li>
 *   <li>totalBalance는 조회 시점에 계산</li>
 *   <li>LedgerEntry 포함 조회 메서드 추가</li>
 * </ul>
 *
 * <h3>메서드 네이밍 컨벤션:</h3>
 * <ul>
 *   <li>{@code findBy...} - 조회, 없으면 Optional.empty()</li>
 *   <li>{@code getBy...} - 조회, 없으면 예외 발생</li>
 *   <li>{@code getOrCreate...} - 조회, 없으면 생성</li>
 *   <li>{@code ...WithAllLedgers} - 모든 Ledger 포함 (적립취소 등)</li>
 *   <li>{@code ...WithAvailableLedgersForUse} - 사용 가능한 Ledger만 (포인트 사용)</li>
 *   <li>{@code ...WithEntries} - LedgerEntry 포함 (v2)</li>
 * </ul>
 */
public interface MemberPointRepository {

    // =====================================================
    // 기본 조회 (레거시 호환)
    // =====================================================

    /**
     * 회원 포인트 조회 (Ledger 미포함)
     * @deprecated v2에서는 findByMemberIdWithAllLedgers() 사용 권장
     */
    @Deprecated
    Optional<MemberPoint> findByMemberId(UUID memberId);

    /**
     * 모든 Ledger를 포함하여 조회
     * - 적립취소 시 사용 (전체 Ledger 필요)
     * - DB에서 earnedAt 역순 정렬
     */
    Optional<MemberPoint> findByMemberIdWithAllLedgers(UUID memberId);

    /**
     * 사용 가능한 Ledger만 포함하여 조회 (최적화)
     * - 포인트 사용 시 사용
     * - DB에서 필터링: 취소되지 않음, 만료되지 않음, 잔액 > 0
     * - DB에서 우선순위 정렬: MANUAL 우선 → 만료일 짧은 순
     */
    Optional<MemberPoint> findByMemberIdWithAvailableLedgersForUse(UUID memberId);

    // =====================================================
    // v2 조회 (LedgerEntry 포함)
    // =====================================================

    /**
     * 회원 포인트 조회 (모든 Ledger + Entry 포함, v2)
     */
    Optional<MemberPoint> findByMemberIdWithAllLedgersAndEntries(UUID memberId);

    /**
     * 사용 가능한 Ledger + Entry 포함 조회 (v2)
     * @param now 현재 시간 (만료 판단용)
     */
    Optional<MemberPoint> findByMemberIdWithAvailableLedgersAndEntries(UUID memberId, LocalDateTime now);

    /**
     * 특정 주문에서 사용된 Ledger + Entry 포함 조회 (v2)
     * - 사용취소 시 사용
     */
    Optional<MemberPoint> findByMemberIdWithLedgersForOrder(UUID memberId, String orderId);

    // =====================================================
    // 저장
    // =====================================================

    /**
     * MemberPoint와 Ledgers 함께 저장
     */
    MemberPoint save(MemberPoint memberPoint);

    /**
     * MemberPoint 저장 (Ledger + Entry 모두 저장, v2)
     */
    MemberPoint saveWithEntries(MemberPoint memberPoint);

    // =====================================================
    // getOrCreate 메서드
    // =====================================================

    /**
     * 회원 포인트 조회, 없으면 생성 (Ledger 미포함)
     * @deprecated v2에서는 getOrCreateWithAllLedgersAndEntries() 사용 권장
     */
    @Deprecated
    MemberPoint getOrCreate(UUID memberId);

    /**
     * 모든 Ledger를 포함하여 조회, 없으면 생성
     */
    MemberPoint getOrCreateWithAllLedgers(UUID memberId);

    /**
     * 사용 가능한 Ledger만 포함하여 조회, 없으면 생성
     */
    MemberPoint getOrCreateWithAvailableLedgersForUse(UUID memberId);

    /**
     * 모든 Ledger + Entry 포함 조회, 없으면 생성 (v2)
     */
    default MemberPoint getOrCreateWithAllLedgersAndEntries(UUID memberId) {
        return findByMemberIdWithAllLedgersAndEntries(memberId)
                .orElseGet(() -> MemberPoint.create(memberId));
    }

    // =====================================================
    // getBy 메서드 (없으면 예외)
    // =====================================================

    /**
     * 모든 Ledger를 포함하여 조회, 없으면 예외 발생
     * @throws MemberPointNotFoundException 회원 포인트가 없는 경우
     */
    default MemberPoint getByMemberIdWithAllLedgers(UUID memberId) {
        return findByMemberIdWithAllLedgers(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }

    /**
     * 모든 Ledger + Entry 포함 조회, 없으면 예외 발생 (v2)
     * @throws MemberPointNotFoundException 회원 포인트가 없는 경우
     */
    default MemberPoint getByMemberIdWithAllLedgersAndEntries(UUID memberId) {
        return findByMemberIdWithAllLedgersAndEntries(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }
}
