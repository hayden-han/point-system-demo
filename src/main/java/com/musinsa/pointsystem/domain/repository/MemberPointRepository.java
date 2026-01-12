package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.util.Optional;
import java.util.UUID;

public interface MemberPointRepository {
    Optional<MemberPoint> findByMemberId(UUID memberId);

    /**
     * Ledgers를 포함하여 조회
     */
    Optional<MemberPoint> findByMemberIdWithLedgers(UUID memberId);

    /**
     * Ledgers를 포함하여 조회, 없으면 MemberPointNotFoundException 발생
     */
    default MemberPoint getByMemberIdWithLedgers(UUID memberId) {
        return findByMemberIdWithLedgers(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }

    /**
     * 사용 가능한 Ledgers만 포함하여 조회 (최적화)
     * - 취소되지 않은 적립건
     * - 만료되지 않은 적립건
     * - 사용 가능 금액이 있는 적립건
     */
    Optional<MemberPoint> findByMemberIdWithAvailableLedgers(UUID memberId);

    /**
     * MemberPoint와 Ledgers 함께 저장
     */
    MemberPoint save(MemberPoint memberPoint);

    MemberPoint getOrCreate(UUID memberId);

    /**
     * Ledgers를 포함하여 조회, 없으면 새로 생성
     */
    MemberPoint getOrCreateWithLedgers(UUID memberId);

    /**
     * 사용 가능한 Ledgers만 포함하여 조회, 없으면 새로 생성
     */
    MemberPoint getOrCreateWithAvailableLedgers(UUID memberId);
}
