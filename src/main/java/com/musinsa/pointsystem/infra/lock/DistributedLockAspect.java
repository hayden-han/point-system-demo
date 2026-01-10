package com.musinsa.pointsystem.infra.lock;

import lombok.RequiredArgsConstructor;
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

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private static final long[] RETRY_DELAYS = {0, 200, 500, 1000};
    private static final int MAX_ATTEMPTS = 4;

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = parseKey(joinPoint, distributedLock.key());
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("락 획득 재시도. lockKey={}, attempt={}, delay={}ms", lockKey, attempt + 1, RETRY_DELAYS[attempt]);
                    Thread.sleep(RETRY_DELAYS[attempt]);
                }

                boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("락 획득 성공. lockKey={}, attempt={}", lockKey, attempt + 1);
                    try {
                        return joinPoint.proceed();
                    } finally {
                        try {
                            if (lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        } catch (Exception e) {
                            log.warn("락 해제 실패. lockKey={}", lockKey, e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockAcquisitionException("락 획득 중 인터럽트 발생", e);
            }
        }

        log.error("분산락 획득 최종 실패. lockKey={}, attempts={}", lockKey, MAX_ATTEMPTS);
        throw new LockAcquisitionException("락 획득 실패: " + lockKey);
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
