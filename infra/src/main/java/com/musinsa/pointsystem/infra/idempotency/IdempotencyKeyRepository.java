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
    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);  // PROCESSING 상태 최대 유지 시간
    private static final String PROCESSING = "PROCESSING";

    private final RedissonClient redissonClient;

    public IdempotencyKeyRepository(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 멱등성 키 상태
     */
    public enum AcquireResult {
        ACQUIRED,           // 새로 획득 (처리 가능)
        ALREADY_COMPLETED,  // 이미 처리 완료
        PROCESSING          // 다른 요청이 처리 중
    }

    /**
     * 멱등성 키가 이미 존재하는지 확인하고, 없으면 저장
     * @return AcquireResult 상태
     */
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

    /**
     * 처리 완료 후 결과 저장
     * - PROCESSING 상태를 결과로 교체하고 TTL을 24시간으로 연장
     */
    public void saveResult(String idempotencyKey, String result) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(result, DEFAULT_TTL);
    }

    /**
     * 저장된 결과 조회
     * @return 처리 완료된 결과 (PROCESSING 상태면 empty)
     */
    public Optional<String> getResult(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();
        if (value == null || PROCESSING.equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /**
     * 현재 상태 확인
     */
    public boolean isProcessing(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        RBucket<String> bucket = redissonClient.getBucket(key);
        return PROCESSING.equals(bucket.get());
    }

    /**
     * 처리 실패 시 키 삭제 (재시도 허용)
     */
    public void remove(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        redissonClient.getBucket(key).delete();
    }
}
