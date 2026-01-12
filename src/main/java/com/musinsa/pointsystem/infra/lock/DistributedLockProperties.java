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
    private long leaseTimeMs = 30000;
    private int maxRetryAttempts = 4;
    private List<Long> retryDelaysMs = List.of(0L, 200L, 500L, 1000L);
}
