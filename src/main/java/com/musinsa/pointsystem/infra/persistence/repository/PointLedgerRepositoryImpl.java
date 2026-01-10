package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointLedgerRepositoryImpl implements PointLedgerRepository {

    private final PointLedgerJpaRepository jpaRepository;
    private final PointLedgerMapper mapper;

    @Override
    public PointLedger save(PointLedger pointLedger) {
        if (pointLedger.getId() != null) {
            PointLedgerEntity entity = jpaRepository.findById(pointLedger.getId())
                    .orElseThrow(() -> new IllegalArgumentException("적립건을 찾을 수 없습니다: " + pointLedger.getId()));
            entity.updateAvailableAmount(pointLedger.getAvailableAmount(), pointLedger.getUsedAmount());
            if (pointLedger.isCanceled()) {
                entity.cancel();
            }
            return mapper.toDomain(jpaRepository.save(entity));
        }
        PointLedgerEntity entity = mapper.toEntity(pointLedger);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PointLedger> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PointLedger> findAvailableByMemberId(Long memberId) {
        return jpaRepository.findAvailableByMemberId(memberId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointLedger> saveAll(List<PointLedger> pointLedgers) {
        return pointLedgers.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }
}
