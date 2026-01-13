package com.musinsa.pointsystem.infra.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * 분산락 관리 서비스
 * - 락 상태 조회
 * - 강제 해제 (운영용)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LockManagementService {

    private final RedissonClient redissonClient;

    /**
     * 락 상태 조회
     */
    public LockInfo getLockInfo(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);

        if (!lock.isLocked()) {
            return LockInfo.notLocked(lockKey);
        }

        return LockInfo.of(
                lockKey,
                true,
                lock.getHoldCount(),
                lock.remainTimeToLive()
        );
    }

    /**
     * 락 강제 해제
     * - 소유자와 관계없이 락을 해제
     * - 운영 목적으로만 사용 (데이터 정합성 주의)
     *
     * @return true: 해제됨, false: 이미 해제된 상태
     */
    public boolean forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);

        if (!lock.isLocked()) {
            log.info("락이 이미 해제된 상태. lockKey={}", lockKey);
            return false;
        }

        log.warn("락 강제 해제 실행! lockKey={}, holdCount={}, remainTTL={}ms",
                lockKey, lock.getHoldCount(), lock.remainTimeToLive());

        lock.forceUnlock();

        log.warn("락 강제 해제 완료. lockKey={}", lockKey);
        return true;
    }

    /**
     * 특정 패턴의 락 목록 조회 (회원 포인트 락)
     */
    public LockInfo getMemberPointLockInfo(String memberId) {
        String lockKey = "lock:point:member:" + memberId;
        return getLockInfo(lockKey);
    }

    /**
     * 회원 포인트 락 강제 해제
     */
    public boolean forceUnlockMemberPoint(String memberId) {
        String lockKey = "lock:point:member:" + memberId;
        return forceUnlock(lockKey);
    }
}
