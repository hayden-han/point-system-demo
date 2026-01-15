package com.musinsa.pointsystem.infra.lock;

import com.musinsa.pointsystem.domain.exception.LockAcquisitionFailedException;
import com.musinsa.pointsystem.domain.model.DistributedLock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final DistributedLockProperties properties;
    private final ExpressionParser parser = new SpelExpressionParser();

    // 메트릭
    private final Counter lockSuccessCounter;
    private final Counter lockFailureCounter;
    private final Counter lockRetryCounter;
    private final Counter lockHoldTimeExceededCounter;
    private final Timer lockAcquireTimer;
    private final Timer lockHoldTimer;

    public DistributedLockAspect(RedissonClient redissonClient,
                                  DistributedLockProperties properties,
                                  MeterRegistry meterRegistry) {
        this.redissonClient = redissonClient;
        this.properties = properties;

        // 메트릭 등록
        this.lockSuccessCounter = Counter.builder("point.lock.acquire")
                .tag("result", "success")
                .description("Number of successful lock acquisitions")
                .register(meterRegistry);

        this.lockFailureCounter = Counter.builder("point.lock.acquire")
                .tag("result", "failure")
                .description("Number of failed lock acquisitions")
                .register(meterRegistry);

        this.lockRetryCounter = Counter.builder("point.lock.retry")
                .description("Number of lock acquisition retries")
                .register(meterRegistry);

        this.lockHoldTimeExceededCounter = Counter.builder("point.lock.hold.exceeded")
                .description("Number of times lock hold time exceeded threshold")
                .register(meterRegistry);

        this.lockAcquireTimer = Timer.builder("point.lock.acquire.duration")
                .description("Time taken to acquire lock")
                .register(meterRegistry);

        this.lockHoldTimer = Timer.builder("point.lock.hold.duration")
                .description("Time the lock was held")
                .register(meterRegistry);
    }

    @Around("@annotation(com.musinsa.pointsystem.domain.port.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);
        String lockKey = parseKey(joinPoint, distributedLock.key());

        // 어노테이션 값이 기본값이면 properties 사용, 아니면 어노테이션 값 사용
        long waitTime = distributedLock.waitTime() != 3000 ? distributedLock.waitTime() : properties.getWaitTimeMs();
        long leaseTime = distributedLock.leaseTime() != 5000 ? distributedLock.leaseTime() : properties.getLeaseTimeMs();

        RLock lock = redissonClient.getLock(lockKey);
        List<Long> retryDelays = properties.getRetryDelaysMs();
        int maxAttempts = properties.getMaxRetryAttempts();

        return lockAcquireTimer.record(() -> {
            try {
                return executeWithRetry(joinPoint, lock, lockKey, waitTime, leaseTime, retryDelays, maxAttempts);
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        });
    }

    private Object executeWithRetry(ProceedingJoinPoint joinPoint, RLock lock, String lockKey,
                                     long waitTime, long leaseTime,
                                     List<Long> retryDelays, int maxAttempts) throws Throwable {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                if (attempt > 0) {
                    long delay = retryDelays.size() > attempt ? retryDelays.get(attempt) : retryDelays.get(retryDelays.size() - 1);
                    log.info("락 획득 재시도. lockKey={}, attempt={}, delay={}ms", lockKey, attempt + 1, delay);
                    lockRetryCounter.increment();
                    Thread.sleep(delay);
                }

                boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("락 획득 성공. lockKey={}, attempt={}", lockKey, attempt + 1);
                    lockSuccessCounter.increment();
                    long holdStartTime = System.currentTimeMillis();
                    try {
                        return joinPoint.proceed();
                    } finally {
                        long holdDuration = System.currentTimeMillis() - holdStartTime;
                        recordHoldTime(lockKey, holdDuration);
                        releaseLock(lock, lockKey);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                lockFailureCounter.increment();
                throw new LockAcquisitionFailedException("락 획득 중 인터럽트 발생", e);
            } catch (Exception e) {
                // Redis 연결 실패 등 예외 처리
                if (isRedisConnectionException(e)) {
                    log.error("Redis 연결 실패. lockKey={}", lockKey, e);
                    lockFailureCounter.increment();
                    throw new LockAcquisitionFailedException("Redis 연결 실패로 락 획득 불가", e);
                }
                throw e;
            }
        }

        log.error("분산락 획득 최종 실패. lockKey={}, attempts={}", lockKey, maxAttempts);
        lockFailureCounter.increment();
        throw new LockAcquisitionFailedException("락 획득 실패: " + lockKey);
    }

    private void recordHoldTime(String lockKey, long holdDurationMs) {
        // 메트릭 기록
        lockHoldTimer.record(holdDurationMs, TimeUnit.MILLISECONDS);

        // 임계값 초과 시 경고
        long threshold = properties.getHoldTimeWarnThresholdMs();
        if (holdDurationMs > threshold) {
            lockHoldTimeExceededCounter.increment();
            log.warn("락 보유 시간 임계값 초과! lockKey={}, holdTime={}ms, threshold={}ms",
                    lockKey, holdDurationMs, threshold);
        }
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

    private boolean isRedisConnectionException(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Unable to connect") ||
               message.contains("Connection refused") ||
               message.contains("RedisConnectionException");
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
