package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.PointPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PointPolicyJpaRepository extends JpaRepository<PointPolicyEntity, UUID> {
    Optional<PointPolicyEntity> findByPolicyKey(String policyKey);
}
