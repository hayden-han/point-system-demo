package com.musinsa.pointsystem.infra.cache;

import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.BalanceCachePort;
import com.musinsa.pointsystem.infra.persistence.repository.PointLedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 잔액 캐시 서비스
 * - Redis 캐시를 활용하여 잔액 조회 성능 최적화
 * - 포인트 변경 시 캐시 무효화 필요
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointBalanceCacheService implements BalanceCachePort {

    private final PointLedgerJpaRepository pointLedgerJpaRepository;

    /**
     * 회원 잔액 조회 (캐시 적용)
     * - TTL: 30초
     * - 캐시 키: memberId
     */
    @Cacheable(value = "memberBalance", key = "#memberId.toString()")
    public PointAmount getTotalBalance(UUID memberId, LocalDateTime now) {
        log.debug("캐시 미스: 회원 잔액 조회. memberId={}", memberId);
        Long balance = pointLedgerJpaRepository.sumAvailableAmount(memberId, now);
        return PointAmount.of(balance != null ? balance : 0L);
    }

    /**
     * 회원 잔액 캐시 무효화
     * - 포인트 적립/사용/취소 시 호출
     */
    @Override
    @CacheEvict(value = "memberBalance", key = "#memberId.toString()")
    public void evictBalanceCache(UUID memberId) {
        log.debug("캐시 무효화: 회원 잔액. memberId={}", memberId);
    }
}
