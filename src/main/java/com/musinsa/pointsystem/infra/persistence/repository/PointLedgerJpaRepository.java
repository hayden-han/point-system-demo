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

    // 만료 처리 방식 변경:
    // - 조회 시점에 expired_at > CURRENT_TIMESTAMP 조건으로 필터링
    // - 별도의 배치 처리 불필요 (스케줄러 제거됨)
    // - 만료된 포인트는 사용 가능한 Ledger 조회에서 자동 제외

    // =====================================================
    // Query 전용 메서드 (Aggregate 로드 없이 DB 직접 조회)
    // =====================================================

    /**
     * 사용 가능한 포인트 총액 (DB에서 직접 계산)
     * <p>
     * Aggregate 로드 없이 SUM 쿼리로 잔액 조회.
     * 잔액 조회 API, 적립 시 최대 잔액 검증에 사용.
     *
     * @param memberId 회원 ID
     * @param now 현재 시간 (만료 판단용)
     * @return 사용 가능한 총 잔액 (null이면 0으로 처리)
     */
    @Query("SELECT COALESCE(SUM(pl.availableAmount), 0) " +
           "FROM PointLedgerEntity pl " +
           "WHERE pl.memberId = :memberId " +
           "AND pl.isCanceled = false " +
           "AND pl.expiredAt > :now " +
           "AND pl.availableAmount > 0")
    Long sumAvailableAmount(@Param("memberId") UUID memberId,
                            @Param("now") LocalDateTime now);

    /**
     * 사용 가능한 Ledger 수 (모니터링/디버깅용)
     *
     * @param memberId 회원 ID
     * @param now 현재 시간 (만료 판단용)
     * @return 사용 가능한 Ledger 수
     */
    @Query("SELECT COUNT(pl) " +
           "FROM PointLedgerEntity pl " +
           "WHERE pl.memberId = :memberId " +
           "AND pl.isCanceled = false " +
           "AND pl.expiredAt > :now " +
           "AND pl.availableAmount > 0")
    int countAvailableLedgers(@Param("memberId") UUID memberId,
                              @Param("now") LocalDateTime now);
}
