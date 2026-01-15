package com.musinsa.pointsystem.infra.idempotency;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "idempotency")
@Getter
@Setter
public class IdempotencyProperties {

    /**
     * 멱등성 키 TTL (초)
     * - 기본값: 600초 (10분)
     * - 네트워크 장애로 인한 클라이언트 재시도는 대부분 수 초~수 분 내 발생
     */
    private long ttlSeconds = 600;

    /**
     * PROCESSING 상태 TTL (초)
     * - 기본값: 30초
     * - 요청 처리 중 서버 장애 시 자동 만료되어 재시도 가능하도록 함
     */
    private long processingTtlSeconds = 30;
}
