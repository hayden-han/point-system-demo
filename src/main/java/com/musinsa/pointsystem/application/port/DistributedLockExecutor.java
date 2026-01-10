package com.musinsa.pointsystem.application.port;

import java.util.function.Supplier;

public interface DistributedLockExecutor {
    <T> T executeWithLock(String lockKey, Supplier<T> task);
    void executeWithLock(String lockKey, Runnable task);
}
