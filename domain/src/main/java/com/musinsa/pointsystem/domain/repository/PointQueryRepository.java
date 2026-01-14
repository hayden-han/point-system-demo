package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointHistory;

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
 *   <li>Spring Data 의존성 없는 순수 도메인 인터페이스</li>
 * </ul>
 *
 * <h3>사용 케이스:</h3>
 * <ul>
 *   <li>잔액 조회 API - Ledger 로드 없이 SUM 쿼리</li>
 *   <li>적립 시 최대 잔액 검증 - 기존 Ledger 수정 불필요</li>
 *   <li>히스토리 조회 - 페이징된 Entry 목록</li>
 * </ul>
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
     * @param pageRequest 페이징 요청
     * @return 페이징된 히스토리
     */
    PageResult<PointHistory> getHistory(UUID memberId, PageRequest pageRequest);
}
