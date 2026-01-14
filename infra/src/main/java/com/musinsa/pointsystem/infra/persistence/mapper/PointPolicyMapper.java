package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.infra.persistence.entity.PointPolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class PointPolicyMapper {

    public PointPolicy toDomain(PointPolicyEntity entity) {
        return new PointPolicy(
                entity.getId(),
                entity.getPolicyKey(),
                entity.getPolicyValue(),
                entity.getDescription()
        );
    }
}
