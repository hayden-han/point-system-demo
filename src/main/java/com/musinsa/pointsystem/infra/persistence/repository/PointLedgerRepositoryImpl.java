package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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
    public List<PointLedger> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointLedger> findAvailableByMemberId(Long memberId) {
        return jpaRepository.findAvailableByMemberId(memberId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointLedger> saveAll(List<PointLedger> pointLedgers) {
        if (pointLedgers.isEmpty()) {
            return List.of();
        }

        // 신규 저장과 업데이트 분리
        List<PointLedger> newLedgers = pointLedgers.stream()
                .filter(ledger -> ledger.getId() == null)
                .toList();
        List<PointLedger> existingLedgers = pointLedgers.stream()
                .filter(ledger -> ledger.getId() != null)
                .toList();

        List<PointLedger> result = new java.util.ArrayList<>();

        // 신규 적립건 배치 저장
        if (!newLedgers.isEmpty()) {
            List<PointLedgerEntity> newEntities = newLedgers.stream()
                    .map(mapper::toEntity)
                    .toList();
            List<PointLedgerEntity> savedEntities = jpaRepository.saveAll(newEntities);
            result.addAll(savedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        // 기존 적립건 배치 업데이트 (한 번의 쿼리로 모든 엔티티 조회 후 업데이트)
        if (!existingLedgers.isEmpty()) {
            List<Long> ids = existingLedgers.stream()
                    .map(PointLedger::getId)
                    .toList();

            // 한 번의 쿼리로 모든 기존 엔티티 조회
            Map<Long, PointLedgerEntity> entityMap = jpaRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

            for (PointLedger ledger : existingLedgers) {
                PointLedgerEntity entity = entityMap.get(ledger.getId());
                if (entity == null) {
                    throw new IllegalArgumentException("적립건을 찾을 수 없습니다: " + ledger.getId());
                }
                entity.updateAvailableAmount(ledger.getAvailableAmount(), ledger.getUsedAmount());
                if (ledger.isCanceled()) {
                    entity.cancel();
                }
            }

            // 변경 감지에 의해 자동으로 업데이트됨 (또는 명시적 saveAll)
            List<PointLedgerEntity> updatedEntities = jpaRepository.saveAll(entityMap.values().stream().toList());
            result.addAll(updatedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        return result;
    }
}
