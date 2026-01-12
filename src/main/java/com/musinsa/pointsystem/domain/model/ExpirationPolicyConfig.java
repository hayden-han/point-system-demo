package com.musinsa.pointsystem.domain.model;

/**
 * 만료 정책 설정 Value Object
 * 사용취소 시 신규 적립에 필요한 만료 정책값 관리
 */
public record ExpirationPolicyConfig(Integer defaultExpirationDays) {

    public static ExpirationPolicyConfig of(Integer defaultExpirationDays) {
        return new ExpirationPolicyConfig(defaultExpirationDays);
    }

    // 기존 코드 호환성을 위한 getter
    public Integer getDefaultExpirationDays() {
        return defaultExpirationDays;
    }
}
