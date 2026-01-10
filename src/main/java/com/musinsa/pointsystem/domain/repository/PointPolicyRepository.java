package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointPolicy;

import java.util.Optional;

public interface PointPolicyRepository {
    Optional<PointPolicy> findByPolicyKey(String policyKey);
    Long getValueByKey(String policyKey);
}
