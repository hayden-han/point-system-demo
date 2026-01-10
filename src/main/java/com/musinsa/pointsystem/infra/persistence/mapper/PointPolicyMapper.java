package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.infra.persistence.entity.PointPolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class PointPolicyMapper {

    public PointPolicy toDomain(PointPolicyEntity entity) {
        return PointPolicy.builder()
                .id(entity.getId())
                .policyKey(entity.getPolicyKey())
                .policyValue(entity.getPolicyValue())
                .description(entity.getDescription())
                .build();
    }
}
