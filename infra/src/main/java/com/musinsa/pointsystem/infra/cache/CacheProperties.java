package com.musinsa.pointsystem.infra.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cache")
@Getter
@Setter
public class CacheProperties {

    /**
     * 정책 캐시 TTL (초)
     * - 기본값: 300초 (5분)
     * - 정책은 자주 변경되지 않으므로 긴 TTL 적용
     */
    private long policyTtlSeconds = 300;

    /**
     * 잔액 캐시 TTL (밀리초)
     * - 기본값: 30000ms (30초)
     * - 포인트 변동 시 캐시 무효화되므로 짧은 TTL로 설정
     */
    private long balanceTtlMs = 30_000;

    /**
     * 잔액 캐시 최대 유휴시간 (밀리초)
     * - 기본값: 10000ms (10초)
     * - 마지막 접근 후 이 시간이 지나면 캐시 만료
     */
    private long balanceMaxIdleMs = 10_000;
}
