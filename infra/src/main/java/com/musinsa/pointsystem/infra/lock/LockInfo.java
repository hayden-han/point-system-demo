package com.musinsa.pointsystem.infra.lock;

import java.time.LocalDateTime;

/**
 * 락 상태 정보 DTO
 */
public record LockInfo(
        String lockKey,
        boolean locked,
        int holdCount,
        long remainTimeToLiveMs,
        LocalDateTime checkedAt
) {
    public static LockInfo of(String lockKey, boolean locked, int holdCount, long remainTimeToLiveMs) {
        return new LockInfo(lockKey, locked, holdCount, remainTimeToLiveMs, LocalDateTime.now());
    }

    public static LockInfo notLocked(String lockKey) {
        return new LockInfo(lockKey, false, 0, -1, LocalDateTime.now());
    }
}
