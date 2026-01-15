package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.EntryType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {

    private final LedgerEntryJpaRepository jpaRepository;
    private final PointLedgerMapper mapper;

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        LedgerEntryEntity entity = mapper.toEntryEntity(entry);
        LedgerEntryEntity saved = jpaRepository.save(entity);
        return mapper.toEntryDomain(saved);
    }

    @Override
    public List<LedgerEntry> saveAll(List<LedgerEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }
        List<LedgerEntryEntity> entities = entries.stream()
                .map(mapper::toEntryEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByLedgerId(UUID ledgerId) {
        return jpaRepository.findByLedgerIdOrderByCreatedAtAsc(ledgerId).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByLedgerIds(List<UUID> ledgerIds) {
        if (ledgerIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByLedgerIdInOrderByCreatedAtAsc(ledgerIds).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByOrderId(String orderId) {
        return jpaRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }

    @Override
    public List<UUID> findLedgerIdsByOrderId(String orderId) {
        return jpaRepository.findDistinctLedgerIdsByOrderId(orderId);
    }

    @Override
    public List<LedgerEntry> findByOrderIdAndType(String orderId, EntryType type) {
        return jpaRepository.findByOrderIdAndType(orderId, type).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }
}
