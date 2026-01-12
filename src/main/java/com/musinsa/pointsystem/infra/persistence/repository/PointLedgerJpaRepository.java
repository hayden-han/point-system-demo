package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PointLedgerJpaRepository extends JpaRepository<PointLedgerEntity, UUID> {

    /**
     * 사용 가능한 적립건 조회 (DB에서 정렬/필터링 완료)
     * - 수기 지급(MANUAL) 우선
     * - 만료일 짧은 순
     *
     * 인덱스 권장: (member_id, is_canceled, available_amount, expired_at)
     */
    @Query("SELECT pl FROM PointLedgerEntity pl " +
           "WHERE pl.memberId = :memberId " +
           "AND pl.availableAmount > 0 " +
           "AND pl.expiredAt > CURRENT_TIMESTAMP " +
           "AND pl.isCanceled = false " +
           "ORDER BY CASE pl.earnType WHEN 'MANUAL' THEN 0 ELSE 1 END, pl.expiredAt ASC")
    List<PointLedgerEntity> findAvailableByMemberIdOrderByPriority(@Param("memberId") UUID memberId);

    /**
     * 회원의 모든 적립건 조회 (만료일 순)
     */
    @Query("SELECT pl FROM PointLedgerEntity pl " +
           "WHERE pl.memberId = :memberId " +
           "ORDER BY pl.earnedAt DESC")
    List<PointLedgerEntity> findByMemberIdOrderByEarnedAtDesc(UUID memberId);

    /**
     * 회원의 모든 적립건 조회
     */
    List<PointLedgerEntity> findByMemberId(UUID memberId);

    /**
     * 만료된 적립건의 잔액을 0으로 설정
     * @param now 현재 시각
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE PointLedgerEntity pl " +
           "SET pl.availableAmount = 0, pl.usedAmount = pl.earnedAmount " +
           "WHERE pl.expiredAt < :now " +
           "AND pl.availableAmount > 0 " +
           "AND pl.isCanceled = false")
    int markExpiredLedgersAsZeroBalance(@Param("now") LocalDateTime now);
}
