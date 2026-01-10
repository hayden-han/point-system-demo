package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 만료 정책 설정 Value Object
 * 사용취소 시 신규 적립에 필요한 만료 정책값 관리
 */
@Getter
@Builder
public class ExpirationPolicyConfig {
    private final Integer defaultExpirationDays;

    public static ExpirationPolicyConfig of(Integer defaultExpirationDays) {
        return ExpirationPolicyConfig.builder()
                .defaultExpirationDays(defaultExpirationDays)
                .build();
    }
}
