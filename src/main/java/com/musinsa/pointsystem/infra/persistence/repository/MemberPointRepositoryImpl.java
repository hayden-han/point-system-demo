package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.MemberPointMapper;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final MemberPointJpaRepository jpaRepository;
    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;
    private final MemberPointMapper mapper;
    private final PointLedgerMapper pointLedgerMapper;

    // =====================================================
    // 레거시 조회 메서드
    // =====================================================

    @Override
    @Deprecated
    public Optional<MemberPoint> findByMemberId(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAllLedgers(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgers = pointLedgerJpaRepository
                            .findByMemberIdOrderByEarnedAtDesc(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgers);
                });
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgersForUse(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgers = pointLedgerJpaRepository
                            .findAvailableByMemberIdOrderByPriority(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgers);
                });
    }

    // =====================================================
    // v2 조회 메서드 (LedgerEntry 포함)
    // =====================================================

    @Override
    public Optional<MemberPoint> findByMemberIdWithAllLedgersAndEntries(UUID memberId) {
        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository
                .findByMemberIdOrderByEarnedAtDesc(memberId);

        if (ledgerEntities.isEmpty()) {
            // MemberPoint 엔티티 존재 여부와 관계없이 빈 ledgers로 생성
            return jpaRepository.findById(memberId)
                    .map(entity -> MemberPoint.of(entity.getMemberId(), List.of()));
        }

        List<UUID> ledgerIds = ledgerEntities.stream()
                .map(PointLedgerEntity::getId)
                .toList();

        Map<UUID, List<LedgerEntryEntity>> entriesByLedgerId = ledgerEntryJpaRepository
                .findByLedgerIdInOrderByCreatedAtAsc(ledgerIds).stream()
                .collect(Collectors.groupingBy(LedgerEntryEntity::getLedgerId));

        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(le -> pointLedgerMapper.toDomain(le, entriesByLedgerId.getOrDefault(le.getId(), List.of())))
                .toList();

        return Optional.of(MemberPoint.of(memberId, ledgers));
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgersAndEntries(UUID memberId, LocalDateTime now) {
        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository
                .findAvailableByMemberIdOrderByPriority(memberId);

        if (ledgerEntities.isEmpty()) {
            return jpaRepository.findById(memberId)
                    .map(entity -> MemberPoint.of(entity.getMemberId(), List.of()));
        }

        List<UUID> ledgerIds = ledgerEntities.stream()
                .map(PointLedgerEntity::getId)
                .toList();

        Map<UUID, List<LedgerEntryEntity>> entriesByLedgerId = ledgerEntryJpaRepository
                .findByLedgerIdInOrderByCreatedAtAsc(ledgerIds).stream()
                .collect(Collectors.groupingBy(LedgerEntryEntity::getLedgerId));

        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(le -> pointLedgerMapper.toDomain(le, entriesByLedgerId.getOrDefault(le.getId(), List.of())))
                .toList();

        return Optional.of(MemberPoint.of(memberId, ledgers));
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithLedgersForOrder(UUID memberId, String orderId) {
        // 해당 주문과 관련된 Ledger ID 조회
        List<UUID> ledgerIds = ledgerEntryJpaRepository.findDistinctLedgerIdsByOrderId(orderId);

        if (ledgerIds.isEmpty()) {
            return Optional.empty();
        }

        // 해당 Ledger 조회
        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findAllById(ledgerIds).stream()
                .filter(le -> le.getMemberId().equals(memberId))
                .toList();

        if (ledgerEntities.isEmpty()) {
            return Optional.empty();
        }

        // Entry 조회
        Map<UUID, List<LedgerEntryEntity>> entriesByLedgerId = ledgerEntryJpaRepository
                .findByLedgerIdInOrderByCreatedAtAsc(ledgerIds).stream()
                .collect(Collectors.groupingBy(LedgerEntryEntity::getLedgerId));

        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(le -> pointLedgerMapper.toDomain(le, entriesByLedgerId.getOrDefault(le.getId(), List.of())))
                .toList();

        return Optional.of(MemberPoint.of(memberId, ledgers));
    }

    // =====================================================
    // 저장 메서드
    // =====================================================

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        // MemberPoint 저장/업데이트 (레거시 호환)
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        MemberPointEntity memberPointEntity = jpaRepository.findById(memberPoint.memberId())
                .map(existing -> {
                    existing.updateTotalBalance(memberPoint.getTotalBalance(now).value());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(memberPoint));

        jpaRepository.save(memberPointEntity);

        // Ledgers 저장
        if (memberPoint.ledgers() != null && !memberPoint.ledgers().isEmpty()) {
            saveLedgers(memberPoint.ledgers());
        }

        return memberPoint;
    }

    @Override
    public MemberPoint saveWithEntries(MemberPoint memberPoint) {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        // MemberPoint 저장/업데이트
        MemberPointEntity memberPointEntity = jpaRepository.findById(memberPoint.memberId())
                .map(existing -> {
                    existing.updateTotalBalance(memberPoint.getTotalBalance(now).value());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(memberPoint));

        jpaRepository.save(memberPointEntity);

        // Ledgers + Entries 저장
        if (memberPoint.ledgers() != null && !memberPoint.ledgers().isEmpty()) {
            saveLedgersWithEntries(memberPoint.ledgers());
        }

        return memberPoint;
    }

    /**
     * Ledgers 배치 저장 (레거시 - Entry 미포함)
     */
    private void saveLedgers(List<PointLedger> ledgers) {
        if (ledgers.isEmpty()) {
            return;
        }

        List<UUID> ids = ledgers.stream()
                .map(PointLedger::id)
                .toList();

        Map<UUID, PointLedgerEntity> existingEntityMap = pointLedgerJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

        List<PointLedgerEntity> newEntities = new ArrayList<>();

        for (PointLedger ledger : ledgers) {
            PointLedgerEntity existingEntity = existingEntityMap.get(ledger.id());
            if (existingEntity != null) {
                existingEntity.updateAvailableAmount(
                        ledger.availableAmount().value(),
                        ledger.usedAmount().value()
                );
                if (ledger.canceled()) {
                    existingEntity.cancel();
                }
            } else {
                newEntities.add(pointLedgerMapper.toEntity(ledger));
            }
        }

        if (!newEntities.isEmpty()) {
            pointLedgerJpaRepository.saveAll(newEntities);
        }
    }

    /**
     * Ledgers + Entries 저장 (v2)
     */
    private void saveLedgersWithEntries(List<PointLedger> ledgers) {
        if (ledgers.isEmpty()) {
            return;
        }

        List<UUID> ids = ledgers.stream()
                .map(PointLedger::id)
                .toList();

        Map<UUID, PointLedgerEntity> existingEntityMap = pointLedgerJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

        // 기존 Entry ID 조회 (중복 저장 방지)
        Set<UUID> existingEntryIds = ledgerEntryJpaRepository.findByLedgerIdInOrderByCreatedAtAsc(ids).stream()
                .map(LedgerEntryEntity::getId)
                .collect(Collectors.toSet());

        List<PointLedgerEntity> newLedgerEntities = new ArrayList<>();
        List<LedgerEntryEntity> newEntryEntities = new ArrayList<>();

        for (PointLedger ledger : ledgers) {
            PointLedgerEntity existingEntity = existingEntityMap.get(ledger.id());

            if (existingEntity != null) {
                // 기존 Ledger 업데이트
                existingEntity.updateAvailableAmount(
                        ledger.availableAmount().value(),
                        ledger.usedAmount().value()
                );
                if (ledger.canceled()) {
                    existingEntity.cancel();
                }
            } else {
                // 신규 Ledger
                newLedgerEntities.add(pointLedgerMapper.toEntity(ledger));
            }

            // 신규 Entry 수집
            for (LedgerEntry entry : ledger.entries()) {
                if (!existingEntryIds.contains(entry.id())) {
                    newEntryEntities.add(pointLedgerMapper.toEntryEntity(entry, ledger.id()));
                }
            }
        }

        // 배치 저장
        if (!newLedgerEntities.isEmpty()) {
            pointLedgerJpaRepository.saveAll(newLedgerEntities);
        }
        if (!newEntryEntities.isEmpty()) {
            ledgerEntryJpaRepository.saveAll(newEntryEntities);
        }
    }

    // =====================================================
    // getOrCreate 메서드
    // =====================================================

    @Override
    @Deprecated
    public MemberPoint getOrCreate(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain)
                .orElseGet(() -> {
                    MemberPoint newMemberPoint = MemberPoint.create(memberId);
                    MemberPointEntity entity = mapper.toEntity(newMemberPoint);
                    jpaRepository.save(entity);
                    return newMemberPoint;
                });
    }

    @Override
    public MemberPoint getOrCreateWithAllLedgers(UUID memberId) {
        return findByMemberIdWithAllLedgers(memberId)
                .orElseGet(() -> createNewMemberPoint(memberId));
    }

    @Override
    public MemberPoint getOrCreateWithAvailableLedgersForUse(UUID memberId) {
        return findByMemberIdWithAvailableLedgersForUse(memberId)
                .orElseGet(() -> createNewMemberPoint(memberId));
    }

    private MemberPoint createNewMemberPoint(UUID memberId) {
        MemberPoint newMemberPoint = MemberPoint.create(memberId);
        MemberPointEntity entity = mapper.toEntity(newMemberPoint);
        jpaRepository.save(entity);
        return newMemberPoint;
    }
}
