package com.musinsa.pointsystem.infra.idempotency;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class IdempotencyKeyRepository {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final RedissonClient redissonClient;

    public IdempotencyKeyRepository(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 멱등성 키가 이미 존재하는지 확인하고, 없으면 저장
     * @return true: 새로 저장됨 (처리 가능), false: 이미 존재 (중복 요청)
     */
    public boolean tryAcquire(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent("PROCESSING", DEFAULT_TTL);
    }

    /**
     * 처리 완료 후 결과 저장
     */
    public void saveResult(String idempotencyKey, String result) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(result, DEFAULT_TTL);
    }

    /**
     * 저장된 결과 조회
     */
    public Optional<String> getResult(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();
        if (value == null || "PROCESSING".equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /**
     * 처리 실패 시 키 삭제 (재시도 허용)
     */
    public void remove(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        redissonClient.getBucket(key).delete();
    }
}
