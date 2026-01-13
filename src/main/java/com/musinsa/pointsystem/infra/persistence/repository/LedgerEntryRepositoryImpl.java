package com.musinsa.pointsystem.infra.persistence.repository;

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
    public LedgerEntry save(LedgerEntry entry, UUID ledgerId) {
        LedgerEntryEntity entity = mapper.toEntryEntity(entry, ledgerId);
        LedgerEntryEntity saved = jpaRepository.save(entity);
        return mapper.toEntryDomain(saved);
    }

    @Override
    public List<LedgerEntry> saveAll(List<LedgerEntry> entries, UUID ledgerId) {
        List<LedgerEntryEntity> entities = entries.stream()
                .map(e -> mapper.toEntryEntity(e, ledgerId))
                .toList();

        List<LedgerEntryEntity> saved = jpaRepository.saveAll(entities);

        return saved.stream()
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
}
