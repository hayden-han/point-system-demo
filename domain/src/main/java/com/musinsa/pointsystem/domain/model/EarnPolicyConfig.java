package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;

/**
 * 적립 정책 설정 Value Object
 * 적립 시 필요한 모든 정책값을 하나로 묶어 관리
 */
public record EarnPolicyConfig(
        PointAmount minAmount,
        PointAmount maxAmount,
        PointAmount maxBalance,
        Integer defaultExpirationDays,
        Integer minExpirationDays,
        Integer maxExpirationDays
) {
    public static EarnPolicyConfig of(
            PointAmount minAmount,
            PointAmount maxAmount,
            PointAmount maxBalance,
            Integer defaultExpirationDays,
            Integer minExpirationDays,
            Integer maxExpirationDays
    ) {
        return new EarnPolicyConfig(
                minAmount, maxAmount, maxBalance,
                defaultExpirationDays, minExpirationDays, maxExpirationDays
        );
    }

    // 기존 코드 호환성을 위한 getter 메서드들
    public PointAmount getMinAmount() {
        return minAmount;
    }

    public PointAmount getMaxAmount() {
        return maxAmount;
    }

    public PointAmount getMaxBalance() {
        return maxBalance;
    }

    public Integer getDefaultExpirationDays() {
        return defaultExpirationDays;
    }

    public Integer getMinExpirationDays() {
        return minExpirationDays;
    }

    public Integer getMaxExpirationDays() {
        return maxExpirationDays;
    }

    public int getExpirationDays(Integer requestedDays) {
        return requestedDays != null ? requestedDays : defaultExpirationDays;
    }

    /**
     * 만료일을 계산합니다.
     * @param requestedDays 요청된 만료일 (일 수), null이면 기본값 사용
     * @param now 기준 시간
     * @return 계산된 만료 일시
     */
    public LocalDateTime calculateExpirationDate(Integer requestedDays, LocalDateTime now) {
        int days = getExpirationDays(requestedDays);
        return now.plusDays(days);
    }
}
