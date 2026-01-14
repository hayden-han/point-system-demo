package com.musinsa.pointsystem.infra.cache;

import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 정책 캐시 Repository
 * - 정책은 자주 변경되지 않으므로 5분 TTL 적용
 * - 정책 변경 시 evict 메서드로 캐시 무효화 가능
 */
@Repository
@RequiredArgsConstructor
public class PolicyCacheRepository {

    private static final String EARN_POLICY_KEY = "cache:policy:earn";
    private static final String EXPIRATION_POLICY_KEY = "cache:policy:expiration";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final RedissonClient redissonClient;

    public Optional<EarnPolicyConfig> getEarnPolicyConfig() {
        RBucket<EarnPolicyConfig> bucket = redissonClient.getBucket(EARN_POLICY_KEY);
        return Optional.ofNullable(bucket.get());
    }

    public void putEarnPolicyConfig(EarnPolicyConfig config) {
        RBucket<EarnPolicyConfig> bucket = redissonClient.getBucket(EARN_POLICY_KEY);
        bucket.set(config, CACHE_TTL);
    }

    public Optional<ExpirationPolicyConfig> getExpirationPolicyConfig() {
        RBucket<ExpirationPolicyConfig> bucket = redissonClient.getBucket(EXPIRATION_POLICY_KEY);
        return Optional.ofNullable(bucket.get());
    }

    public void putExpirationPolicyConfig(ExpirationPolicyConfig config) {
        RBucket<ExpirationPolicyConfig> bucket = redissonClient.getBucket(EXPIRATION_POLICY_KEY);
        bucket.set(config, CACHE_TTL);
    }

    /**
     * 정책 변경 시 캐시 무효화
     */
    public void evictAll() {
        redissonClient.getBucket(EARN_POLICY_KEY).delete();
        redissonClient.getBucket(EXPIRATION_POLICY_KEY).delete();
    }

    public void evictEarnPolicyConfig() {
        redissonClient.getBucket(EARN_POLICY_KEY).delete();
    }

    public void evictExpirationPolicyConfig() {
        redissonClient.getBucket(EXPIRATION_POLICY_KEY).delete();
    }
}
