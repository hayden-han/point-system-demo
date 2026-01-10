package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.infra.persistence.mapper.PointPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointPolicyRepositoryImpl implements PointPolicyRepository {

    private final PointPolicyJpaRepository jpaRepository;
    private final PointPolicyMapper mapper;

    @Override
    public Optional<PointPolicy> findByPolicyKey(String policyKey) {
        return jpaRepository.findByPolicyKey(policyKey)
                .map(mapper::toDomain);
    }

    @Override
    public Long getValueByKey(String policyKey) {
        return jpaRepository.findByPolicyKey(policyKey)
                .map(entity -> entity.getPolicyValue())
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + policyKey));
    }
}
