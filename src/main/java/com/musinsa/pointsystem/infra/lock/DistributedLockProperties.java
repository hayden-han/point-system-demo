package com.musinsa.pointsystem.infra.lock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "distributed-lock")
@Getter
@Setter
public class DistributedLockProperties {

    private long waitTimeMs = 3000;
    private long leaseTimeMs = 300000;  // 5분 TTL (긴 TTL + 모니터링 방식)
    private int maxRetryAttempts = 4;
    private List<Long> retryDelaysMs = List.of(0L, 200L, 500L, 1000L);
    private long holdTimeWarnThresholdMs = 3000;  // 락 보유 시간 경고 임계값 (예상 시간 × 2)
}
