package com.musinsa.pointsystem.domain.repository;

import java.util.UUID;

/**
 * 잔액 캐시 포트
 * - 포인트 변경 시 캐시 무효화를 위한 인터페이스
 */
public interface BalanceCachePort {

    /**
     * 회원 잔액 캐시 무효화
     */
    void evictBalanceCache(UUID memberId);
}
