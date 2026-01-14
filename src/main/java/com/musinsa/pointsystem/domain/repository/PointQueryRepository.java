package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointAmount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 조회 전용 Repository
 *
 * <h3>설계 원칙:</h3>
 * <ul>
 *   <li>Aggregate 로드 없이 DB 직접 조회 (성능 최적화)</li>
 *   <li>읽기 전용 - 상태 변경 메서드 없음</li>
 *   <li>조회/명령 Repository 분리를 통한 책임 분리</li>
 * </ul>
 *
 * <h3>사용 케이스:</h3>
 * <ul>
 *   <li>잔액 조회 API - Ledger 로드 없이 SUM 쿼리</li>
 *   <li>적립 시 최대 잔액 검증 - 기존 Ledger 수정 불필요</li>
 *   <li>히스토리 조회 - 페이징된 Entry 목록</li>
 * </ul>
 *
 * <p>참고: 이는 CQRS 패턴이 아닙니다. CQRS는 읽기/쓰기 데이터 저장소를 물리적으로 분리하고
 * 최종 일관성(Eventual Consistency)을 수용하는 아키텍처입니다.
 * 본 프로젝트는 단일 DB를 사용하며, Repository 레벨에서 조회/명령 책임만 분리한 구조입니다.
 *
 * @see MemberPointRepository Command용 Repository (Aggregate 로드/저장)
 */
public interface PointQueryRepository {

    /**
     * 회원의 현재 사용 가능한 총 잔액 조회
     * <p>
     * DB에서 SUM 쿼리로 직접 계산하여 Ledger 로드 없이 잔액 반환.
     * 만료되지 않고, 취소되지 않은 Ledger의 availableAmount 합계.
     *
     * @param memberId 회원 ID
     * @param now 현재 시간 (만료 판단용)
     * @return 사용 가능한 총 잔액 (Ledger가 없으면 0)
     */
    PointAmount getTotalBalance(UUID memberId, LocalDateTime now);

    /**
     * 회원의 사용 가능한 Ledger 수 조회
     * <p>
     * 모니터링 및 디버깅 용도.
     *
     * @param memberId 회원 ID
     * @param now 현재 시간 (만료 판단용)
     * @return 사용 가능한 Ledger 수
     */
    int getAvailableLedgerCount(UUID memberId, LocalDateTime now);

    /**
     * 회원의 포인트 변동 이력 페이징 조회
     * <p>
     * Aggregate 로드 없이 DB에서 직접 조회.
     * 최신 순으로 정렬.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 히스토리 Projection
     */
    Page<PointHistoryProjection> getHistory(UUID memberId, Pageable pageable);

    /**
     * 포인트 히스토리 조회 결과 Projection
     */
    interface PointHistoryProjection {
        UUID getEntryId();
        UUID getLedgerId();
        String getType();
        long getAmount();
        String getOrderId();
        LocalDateTime getCreatedAt();
    }
}
