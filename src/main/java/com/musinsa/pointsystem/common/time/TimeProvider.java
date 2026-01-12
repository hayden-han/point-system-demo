package com.musinsa.pointsystem.common.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 시스템 전역 시간 제공자
 * - 모든 시간 관련 로직에서 UTC 기준 사용
 * - 테스트 시 Mock 가능하도록 인터페이스 제공
 */
public interface TimeProvider {

    /**
     * 현재 시간 (UTC)
     */
    LocalDateTime now();

    /**
     * 현재 시간 기준 만료 여부 확인
     */
    default boolean isExpired(LocalDateTime expiredAt) {
        return expiredAt.isBefore(now());
    }

    /**
     * 현재 시간 + N일 (만료일 계산용)
     */
    default LocalDateTime plusDays(int days) {
        return now().plusDays(days);
    }

    /**
     * 기본 구현 - UTC 기준
     */
    static TimeProvider system() {
        return new SystemTimeProvider();
    }
}
