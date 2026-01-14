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
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointLedgerRepositoryImpl implements PointLedgerRepository {

    private final PointLedgerJpaRepository jpaRepository;
    private final LedgerEntryJpaRepository entryJpaRepository;
    private final PointLedgerMapper mapper;

    @Override
    public PointLedger save(PointLedger pointLedger) {
        if (pointLedger.id() != null) {
            // UUIDv7을 사용하므로 ID가 항상 존재함. DB 조회로 신규/기존 구분
            Optional<PointLedgerEntity> existingEntity = jpaRepository.findById(pointLedger.id());
            if (existingEntity.isPresent()) {
                // 기존 엔티티 업데이트
                PointLedgerEntity entity = existingEntity.get();
                entity.updateAvailableAmount(pointLedger.availableAmount().value(), pointLedger.usedAmount().value());
                if (pointLedger.canceled()) {
                    entity.cancel();
                }
                return mapper.toDomain(jpaRepository.save(entity));
            }
        }
        // 신규 엔티티 저장
        PointLedgerEntity entity = mapper.toEntity(pointLedger);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PointLedger> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PointLedger> findByIdWithEntries(UUID id) {
        return jpaRepository.findById(id)
                .map(entity -> {
                    var entries = entryJpaRepository.findByLedgerIdOrderByCreatedAtAsc(id);
                    return mapper.toDomain(entity, entries);
                });
    }

    @Override
    public List<PointLedger> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointLedger> findAvailableByMemberId(UUID memberId) {
        return jpaRepository.findAvailableByMemberIdOrderByPriority(memberId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointLedger> saveAll(List<PointLedger> pointLedgers) {
        if (pointLedgers.isEmpty()) {
            return List.of();
        }

        // ID가 이미 생성되어 있으므로 JPA에서 새 엔티티인지 확인하려면 영속성 컨텍스트 조회 필요
        // 도메인 모델에서 ID가 생성되므로 모든 엔티티를 새 엔티티로 처리하거나,
        // 별도의 신규/기존 구분 로직 필요

        List<UUID> ids = pointLedgers.stream()
                .map(PointLedger::id)
                .toList();

        // 기존 엔티티 조회
        Map<UUID, PointLedgerEntity> existingEntityMap = jpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

        List<PointLedger> result = new java.util.ArrayList<>();

        List<PointLedgerEntity> newEntities = new java.util.ArrayList<>();

        for (PointLedger ledger : pointLedgers) {
            PointLedgerEntity existingEntity = existingEntityMap.get(ledger.id());
            if (existingEntity != null) {
                // 기존 엔티티 업데이트
                existingEntity.updateAvailableAmount(ledger.availableAmount().value(), ledger.usedAmount().value());
                if (ledger.canceled()) {
                    existingEntity.cancel();
                }
            } else {
                // 신규 엔티티
                newEntities.add(mapper.toEntity(ledger));
            }
        }

        // 신규 엔티티 배치 저장
        if (!newEntities.isEmpty()) {
            List<PointLedgerEntity> savedEntities = jpaRepository.saveAll(newEntities);
            result.addAll(savedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        // 기존 엔티티 배치 저장 (변경감지로 업데이트됨)
        if (!existingEntityMap.isEmpty()) {
            List<PointLedgerEntity> updatedEntities = jpaRepository.saveAll(existingEntityMap.values().stream().toList());
            result.addAll(updatedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        return result;
    }
}
