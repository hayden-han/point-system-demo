package com.musinsa.pointsystem.infra.idempotency;

import com.musinsa.pointsystem.domain.infrastructure.IdempotencyKeyPort;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 멱등성 키 관리 구현체
 */
@Repository
public class IdempotencyKeyRepository implements IdempotencyKeyPort {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);  // PROCESSING 상태 최대 유지 시간
    private static final String PROCESSING = "PROCESSING";

    private final RedissonClient redissonClient;
    private final IdempotencyProperties properties;

    public IdempotencyKeyRepository(RedissonClient redissonClient, IdempotencyProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }

    @Override
    public AcquireResult tryAcquire(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);

        // PROCESSING 상태로 설정 시도 (짧은 TTL로 설정하여 처리 실패 시 자동 만료)
        boolean acquired = bucket.setIfAbsent(PROCESSING, PROCESSING_TTL);
        if (acquired) {
            return AcquireResult.ACQUIRED;
        }

        // 이미 존재하는 경우, 상태 확인
        String value = bucket.get();
        if (value == null) {
            // 다른 스레드가 삭제했거나 만료됨 → 재시도
            return tryAcquire(idempotencyKey);
        }
        if (PROCESSING.equals(value)) {
            return AcquireResult.PROCESSING;
        }
        return AcquireResult.ALREADY_COMPLETED;
    }

    @Override
    public void saveResult(String idempotencyKey, String result) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(result, Duration.ofSeconds(properties.getTtlSeconds()));
    }

    @Override
    public Optional<String> getResult(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();
        if (value == null || PROCESSING.equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public void remove(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        redissonClient.getBucket(key).delete();
    }
}
