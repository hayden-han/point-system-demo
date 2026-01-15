package com.musinsa.pointsystem.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 포인트 연산 메트릭 수집기
 * - 적립/사용/취소 연산별 처리 시간 및 횟수 추적
 * - Prometheus/Grafana에서 모니터링 가능
 */
@Component
@Slf4j
public class PointOperationMetrics {

    private final Timer earnTimer;
    private final Timer useTimer;
    private final Timer cancelUseTimer;

    private final Counter earnCounter;
    private final Counter useCounter;
    private final Counter cancelUseCounter;

    private final Counter earnErrorCounter;
    private final Counter useErrorCounter;
    private final Counter cancelUseErrorCounter;

    public PointOperationMetrics(MeterRegistry meterRegistry) {
        // 타이머 (처리 시간 측정)
        this.earnTimer = Timer.builder("point.operation.duration")
                .tag("operation", "earn")
                .description("포인트 적립 처리 시간")
                .register(meterRegistry);

        this.useTimer = Timer.builder("point.operation.duration")
                .tag("operation", "use")
                .description("포인트 사용 처리 시간")
                .register(meterRegistry);

        this.cancelUseTimer = Timer.builder("point.operation.duration")
                .tag("operation", "cancel_use")
                .description("포인트 사용취소 처리 시간")
                .register(meterRegistry);

        // 카운터 (성공 횟수)
        this.earnCounter = Counter.builder("point.operation.count")
                .tag("operation", "earn")
                .tag("result", "success")
                .description("포인트 적립 성공 횟수")
                .register(meterRegistry);

        this.useCounter = Counter.builder("point.operation.count")
                .tag("operation", "use")
                .tag("result", "success")
                .description("포인트 사용 성공 횟수")
                .register(meterRegistry);

        this.cancelUseCounter = Counter.builder("point.operation.count")
                .tag("operation", "cancel_use")
                .tag("result", "success")
                .description("포인트 사용취소 성공 횟수")
                .register(meterRegistry);

        // 에러 카운터
        this.earnErrorCounter = Counter.builder("point.operation.count")
                .tag("operation", "earn")
                .tag("result", "error")
                .description("포인트 적립 실패 횟수")
                .register(meterRegistry);

        this.useErrorCounter = Counter.builder("point.operation.count")
                .tag("operation", "use")
                .tag("result", "error")
                .description("포인트 사용 실패 횟수")
                .register(meterRegistry);

        this.cancelUseErrorCounter = Counter.builder("point.operation.count")
                .tag("operation", "cancel_use")
                .tag("result", "error")
                .description("포인트 사용취소 실패 횟수")
                .register(meterRegistry);
    }

    public void recordEarnSuccess(long durationMs) {
        earnTimer.record(durationMs, TimeUnit.MILLISECONDS);
        earnCounter.increment();
    }

    public void recordEarnError() {
        earnErrorCounter.increment();
    }

    public void recordUseSuccess(long durationMs) {
        useTimer.record(durationMs, TimeUnit.MILLISECONDS);
        useCounter.increment();
    }

    public void recordUseError() {
        useErrorCounter.increment();
    }

    public void recordCancelUseSuccess(long durationMs) {
        cancelUseTimer.record(durationMs, TimeUnit.MILLISECONDS);
        cancelUseCounter.increment();
    }

    public void recordCancelUseError() {
        cancelUseErrorCounter.increment();
    }
}
