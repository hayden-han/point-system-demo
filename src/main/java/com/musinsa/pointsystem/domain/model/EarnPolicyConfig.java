package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 적립 정책 설정 Value Object
 * 적립 시 필요한 모든 정책값을 하나로 묶어 관리
 */
@Getter
@Builder
public class EarnPolicyConfig {
    private final PointAmount minAmount;
    private final PointAmount maxAmount;
    private final PointAmount maxBalance;
    private final Integer defaultExpirationDays;
    private final Integer minExpirationDays;
    private final Integer maxExpirationDays;

    public int getExpirationDays(Integer requestedDays) {
        return requestedDays != null ? requestedDays : defaultExpirationDays;
    }

    /**
     * 만료일을 계산합니다.
     * @param requestedDays 요청된 만료일 (일 수), null이면 기본값 사용
     * @return 계산된 만료 일시
     */
    public LocalDateTime calculateExpirationDate(Integer requestedDays) {
        int days = getExpirationDays(requestedDays);
        return LocalDateTime.now().plusDays(days);
    }
}
