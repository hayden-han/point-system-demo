package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.util.Optional;
import java.util.UUID;

/**
 * MemberPoint Repository 인터페이스
 *
 * <h3>메서드 네이밍 컨벤션:</h3>
 * <ul>
 *   <li>{@code findBy...} - 조회, 없으면 Optional.empty()</li>
 *   <li>{@code getBy...} - 조회, 없으면 예외 발생</li>
 *   <li>{@code getOrCreate...} - 조회, 없으면 생성</li>
 *   <li>{@code ...WithAllLedgers} - 모든 Ledger 포함 (적립취소 등)</li>
 *   <li>{@code ...WithAvailableLedgersForUse} - 사용 가능한 Ledger만 (포인트 사용)</li>
 * </ul>
 */
public interface MemberPointRepository {

    /**
     * 회원 포인트 조회 (Ledger 미포함)
     */
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

    /**
     * MemberPoint와 Ledgers 함께 저장
     */
    MemberPoint save(MemberPoint memberPoint);

    /**
     * 회원 포인트 조회, 없으면 생성 (Ledger 미포함)
     */
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
     * 모든 Ledger를 포함하여 조회, 없으면 예외 발생
     * @throws MemberPointNotFoundException 회원 포인트가 없는 경우
     */
    default MemberPoint getByMemberIdWithAllLedgers(UUID memberId) {
        return findByMemberIdWithAllLedgers(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }
}
