package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.PointPolicy;

import java.util.Optional;

public interface PointPolicyRepository {
    Optional<PointPolicy> findByPolicyKey(String policyKey);
    Long getValueByKey(String policyKey);

    /**
     * 적립 관련 정책을 한 번에 조회
     */
    EarnPolicyConfig getEarnPolicyConfig();

    /**
     * 만료 관련 정책을 한 번에 조회
     */
    ExpirationPolicyConfig getExpirationPolicyConfig();
}
