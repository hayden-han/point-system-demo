package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 적립 정책 설정 Value Object
 * 적립 시 필요한 모든 정책값을 하나로 묶어 관리
 */
@Getter
@Builder
public class EarnPolicyConfig {
    private final Long minAmount;
    private final Long maxAmount;
    private final Long maxBalance;
    private final Integer defaultExpirationDays;
    private final Integer minExpirationDays;
    private final Integer maxExpirationDays;

    public int getExpirationDays(Integer requestedDays) {
        return requestedDays != null ? requestedDays : defaultExpirationDays;
    }
}
