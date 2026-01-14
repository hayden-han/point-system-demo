package com.musinsa.pointsystem.domain.model;

import java.util.UUID;

/**
 * 포인트 정책 설정값
 * - 불변 record
 * - 정책 키 상수 정의
 */
public record PointPolicy(
        UUID id,
        String policyKey,
        Long policyValue,
        String description
) {
    // 정책 키 상수
    public static final String EARN_MIN_AMOUNT = "EARN_MIN_AMOUNT";
    public static final String EARN_MAX_AMOUNT = "EARN_MAX_AMOUNT";
    public static final String BALANCE_MAX_AMOUNT = "BALANCE_MAX_AMOUNT";
    public static final String EXPIRATION_DEFAULT_DAYS = "EXPIRATION_DEFAULT_DAYS";
    public static final String EXPIRATION_MIN_DAYS = "EXPIRATION_MIN_DAYS";
    public static final String EXPIRATION_MAX_DAYS = "EXPIRATION_MAX_DAYS";

    // 기존 코드 호환성을 위한 getter 메서드들
    public UUID getId() {
        return id;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public Long getPolicyValue() {
        return policyValue;
    }

    public String getDescription() {
        return description;
    }
}
