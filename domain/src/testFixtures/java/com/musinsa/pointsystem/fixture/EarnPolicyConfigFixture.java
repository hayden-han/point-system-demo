package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.PointAmount;

/**
 * EarnPolicyConfig 테스트 픽스처
 */
public class EarnPolicyConfigFixture {

    /**
     * 기본 적립 정책
     * - 최소 적립금: 1원
     * - 최대 적립금: 1,000,000원
     * - 최대 보유금: 10,000,000원
     * - 기본 만료일: 365일
     * - 최소 만료일: 1일
     * - 최대 만료일: 730일
     */
    public static EarnPolicyConfig defaultConfig() {
        return EarnPolicyConfig.of(
                PointAmount.of(1L),
                PointAmount.of(1_000_000L),
                PointAmount.of(10_000_000L),
                365,
                1,
                730
        );
    }

    /**
     * 테스트용 엄격한 정책
     */
    public static EarnPolicyConfig strictConfig() {
        return EarnPolicyConfig.of(
                PointAmount.of(100L),
                PointAmount.of(10_000L),
                PointAmount.of(100_000L),
                30,
                1,
                90
        );
    }

    /**
     * 테스트용 느슨한 정책
     */
    public static EarnPolicyConfig relaxedConfig() {
        return EarnPolicyConfig.of(
                PointAmount.of(1L),
                PointAmount.of(100_000_000L),
                PointAmount.of(1_000_000_000L),
                1095,
                1,
                3650
        );
    }
}
