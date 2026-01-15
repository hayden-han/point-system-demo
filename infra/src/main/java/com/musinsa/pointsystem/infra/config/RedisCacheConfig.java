package com.musinsa.pointsystem.infra.config;

import com.musinsa.pointsystem.infra.cache.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final CacheProperties cacheProperties;

    /**
     * Redis 기반 캐시 매니저
     * - memberBalance: 회원 잔액 캐시
     */
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, org.redisson.spring.cache.CacheConfig> config = new HashMap<>();

        // memberBalance 캐시: 포인트 변경 시 캐시 무효화 필요
        config.put("memberBalance", new org.redisson.spring.cache.CacheConfig(
                cacheProperties.getBalanceTtlMs(),
                cacheProperties.getBalanceMaxIdleMs()
        ));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
