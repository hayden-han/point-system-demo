package com.musinsa.pointsystem.domain.port;

import java.time.LocalDateTime;

/**
 * 시간 제공 포트 (도메인 레이어)
 * - 도메인이 인프라에 의존하지 않도록 추상화
 * - 구현체는 인프라 레이어에서 제공
 * - 모든 시간은 UTC 기준
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
}
