package com.musinsa.pointsystem.infra.lock;

import com.musinsa.pointsystem.application.exception.LockAcquisitionFailedException;
import com.musinsa.pointsystem.application.port.DistributedLockExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockExecutor implements DistributedLockExecutor {

    private static final long[] RETRY_DELAYS = {0, 200, 500, 1000};
    private static final int MAX_ATTEMPTS = 4;
    private static final long DEFAULT_WAIT_TIME = 3000;
    private static final long DEFAULT_LEASE_TIME = 5000;

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("락 획득 재시도. lockKey={}, attempt={}, delay={}ms", lockKey, attempt + 1, RETRY_DELAYS[attempt]);
                    Thread.sleep(RETRY_DELAYS[attempt]);
                }

                boolean acquired = lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("락 획득 성공. lockKey={}, attempt={}", lockKey, attempt + 1);
                    try {
                        return task.get();
                    } finally {
                        releaseLock(lock, lockKey);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockAcquisitionFailedException("락 획득 중 인터럽트 발생", e);
            }
        }

        log.error("분산락 획득 최종 실패. lockKey={}, attempts={}", lockKey, MAX_ATTEMPTS);
        throw new LockAcquisitionFailedException("락 획득 실패: " + lockKey);
    }

    @Override
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, () -> {
            task.run();
            return null;
        });
    }

    private void releaseLock(RLock lock, String lockKey) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("락 해제 실패. lockKey={}", lockKey, e);
        }
    }
}
