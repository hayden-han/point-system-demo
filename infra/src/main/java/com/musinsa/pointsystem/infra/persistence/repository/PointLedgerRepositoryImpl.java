package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PointLedgerRepositoryImpl implements PointLedgerRepository {

    private final PointLedgerJpaRepository jpaRepository;
    private final PointLedgerMapper mapper;

    @Override
    public Optional<PointLedger> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PointLedger> findAvailableByMemberId(UUID memberId, LocalDateTime now) {
        return jpaRepository.findAvailableByMemberIdOrderByPriority(memberId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PointLedger> findAllByMemberId(UUID memberId) {
        return jpaRepository.findByMemberId(memberId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PointLedger> findAllByIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PointLedger save(PointLedger ledger) {
        PointLedgerEntity entity = mapper.toEntity(ledger);
        PointLedgerEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<PointLedger> saveAll(List<PointLedger> ledgers) {
        if (ledgers.isEmpty()) {
            return List.of();
        }
        List<PointLedgerEntity> entities = ledgers.stream()
                .map(mapper::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
