package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointPolicyEntity;
import com.musinsa.pointsystem.infra.persistence.entity.QPointPolicyEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointPolicyMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointPolicyRepositoryImpl implements PointPolicyRepository {

    private final PointPolicyJpaRepository jpaRepository;
    private final PointPolicyMapper mapper;
    private final JPAQueryFactory queryFactory;

    private static final QPointPolicyEntity pointPolicy = QPointPolicyEntity.pointPolicyEntity;

    @Override
    public Optional<PointPolicy> findByPolicyKey(String policyKey) {
        return jpaRepository.findByPolicyKey(policyKey)
                .map(mapper::toDomain);
    }

    @Override
    public Long getValueByKey(String policyKey) {
        return jpaRepository.findByPolicyKey(policyKey)
                .map(PointPolicyEntity::getPolicyValue)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + policyKey));
    }

    @Override
    public EarnPolicyConfig getEarnPolicyConfig() {
        List<String> keys = List.of(
                PointPolicy.EARN_MIN_AMOUNT,
                PointPolicy.EARN_MAX_AMOUNT,
                PointPolicy.BALANCE_MAX_AMOUNT,
                PointPolicy.EXPIRATION_DEFAULT_DAYS,
                PointPolicy.EXPIRATION_MIN_DAYS,
                PointPolicy.EXPIRATION_MAX_DAYS
        );

        Map<String, Long> policyMap = queryFactory
                .selectFrom(pointPolicy)
                .where(pointPolicy.policyKey.in(keys))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        PointPolicyEntity::getPolicyKey,
                        PointPolicyEntity::getPolicyValue
                ));

        return new EarnPolicyConfig(
                PointAmount.of(policyMap.get(PointPolicy.EARN_MIN_AMOUNT)),
                PointAmount.of(policyMap.get(PointPolicy.EARN_MAX_AMOUNT)),
                PointAmount.of(policyMap.get(PointPolicy.BALANCE_MAX_AMOUNT)),
                policyMap.get(PointPolicy.EXPIRATION_DEFAULT_DAYS).intValue(),
                policyMap.get(PointPolicy.EXPIRATION_MIN_DAYS).intValue(),
                policyMap.get(PointPolicy.EXPIRATION_MAX_DAYS).intValue()
        );
    }

    @Override
    public ExpirationPolicyConfig getExpirationPolicyConfig() {
        Long defaultDays = getValueByKey(PointPolicy.EXPIRATION_DEFAULT_DAYS);
        return ExpirationPolicyConfig.of(defaultDays.intValue());
    }
}
