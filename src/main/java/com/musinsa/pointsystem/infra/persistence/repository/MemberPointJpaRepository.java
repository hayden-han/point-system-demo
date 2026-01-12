package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MemberPointJpaRepository extends JpaRepository<MemberPointEntity, UUID> {

    /**
     * MemberPoint와 모든 Ledgers를 함께 조회 (Fetch Join)
     */
    @Query("SELECT m FROM MemberPointEntity m LEFT JOIN FETCH m.ledgers WHERE m.memberId = :memberId")
    Optional<MemberPointEntity> findByIdWithLedgers(@Param("memberId") UUID memberId);

    /**
     * MemberPoint와 사용 가능한 Ledgers만 함께 조회 (Fetch Join)
     * - availableAmount > 0
     * - 만료되지 않음
     * - 취소되지 않음
     */
    @Query("""
        SELECT m FROM MemberPointEntity m
        LEFT JOIN FETCH m.ledgers l
        WHERE m.memberId = :memberId
        AND (l IS NULL OR (l.availableAmount > 0 AND l.expiredAt > CURRENT_TIMESTAMP AND l.isCanceled = false))
        ORDER BY CASE l.earnType WHEN 'MANUAL' THEN 0 ELSE 1 END, l.expiredAt ASC
        """)
    Optional<MemberPointEntity> findByIdWithAvailableLedgers(@Param("memberId") UUID memberId);
}
