package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.time.TimeProvider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 테스트용 고정 시간 제공자
 * - 테스트에서 시간을 고정하여 재현 가능한 테스트 작성 가능
 */
public class FixedTimeProvider implements TimeProvider {

    private LocalDateTime fixedTime;

    private FixedTimeProvider(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    /**
     * UTC 기준 2024-01-15 10:00:00 고정 시간
     */
    public static FixedTimeProvider create() {
        return new FixedTimeProvider(LocalDateTime.of(2024, 1, 15, 10, 0, 0));
    }

    /**
     * 지정된 시간으로 생성
     */
    public static FixedTimeProvider of(LocalDateTime fixedTime) {
        return new FixedTimeProvider(fixedTime);
    }

    /**
     * 현재 시간 기준 생성 (UTC)
     */
    public static FixedTimeProvider fromNow() {
        return new FixedTimeProvider(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime now() {
        return fixedTime;
    }

    /**
     * 시간 변경 (테스트 시나리오용)
     */
    public void setFixedTime(LocalDateTime newTime) {
        this.fixedTime = newTime;
    }

    /**
     * N일 후로 시간 이동
     */
    public void advanceDays(int days) {
        this.fixedTime = this.fixedTime.plusDays(days);
    }

    /**
     * N시간 후로 시간 이동
     */
    public void advanceHours(int hours) {
        this.fixedTime = this.fixedTime.plusHours(hours);
    }

    /**
     * N분 후로 시간 이동
     */
    public void advanceMinutes(int minutes) {
        this.fixedTime = this.fixedTime.plusMinutes(minutes);
    }
}
