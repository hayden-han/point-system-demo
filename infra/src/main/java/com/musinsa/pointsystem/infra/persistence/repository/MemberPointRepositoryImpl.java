package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MemberPointRepository 구현체 (entries 기반)
 * - member_point 테이블 없음
 * - point_ledger + ledger_entry로 MemberPoint 구성
 */
@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;
    private final PointLedgerMapper pointLedgerMapper;

    // =====================================================
    // 조회 메서드
    // =====================================================

    @Override
    public Optional<MemberPoint> findByMemberIdWithAllLedgersAndEntries(UUID memberId) {
        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository
                .findByMemberIdOrderByEarnedAtDesc(memberId);

        if (ledgerEntities.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(buildMemberPointWithEntries(memberId, ledgerEntities));
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgersAndEntries(UUID memberId, LocalDateTime now) {
        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository
                .findAvailableByMemberIdOrderByPriority(memberId);

        if (ledgerEntities.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(buildMemberPointWithEntries(memberId, ledgerEntities));
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithLedgersForOrder(UUID memberId, String orderId) {
        List<UUID> ledgerIds = ledgerEntryJpaRepository.findDistinctLedgerIdsByOrderId(orderId);

        if (ledgerIds.isEmpty()) {
            return Optional.empty();
        }

        List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findAllById(ledgerIds).stream()
                .filter(le -> le.getMemberId().equals(memberId))
                .toList();

        if (ledgerEntities.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(buildMemberPointWithEntries(memberId, ledgerEntities));
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithSpecificLedger(UUID memberId, UUID ledgerId) {
        return pointLedgerJpaRepository.findById(ledgerId)
                .filter(le -> le.getMemberId().equals(memberId))
                .map(ledgerEntity -> buildMemberPointWithEntries(memberId, List.of(ledgerEntity)));
    }

    private MemberPoint buildMemberPointWithEntries(UUID memberId, List<PointLedgerEntity> ledgerEntities) {
        List<UUID> ledgerIds = ledgerEntities.stream()
                .map(PointLedgerEntity::getId)
                .toList();

        Map<UUID, List<LedgerEntryEntity>> entriesByLedgerId = ledgerEntryJpaRepository
                .findByLedgerIdInOrderByCreatedAtAsc(ledgerIds).stream()
                .collect(Collectors.groupingBy(LedgerEntryEntity::getLedgerId));

        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(le -> pointLedgerMapper.toDomain(le, entriesByLedgerId.getOrDefault(le.getId(), List.of())))
                .toList();

        return MemberPoint.of(memberId, ledgers);
    }

    // =====================================================
    // getOrCreate 메서드
    // =====================================================

    @Override
    public MemberPoint getOrCreateWithAllLedgersAndEntries(UUID memberId) {
        return findByMemberIdWithAllLedgersAndEntries(memberId)
                .orElseGet(() -> MemberPoint.create(memberId));
    }

    public MemberPoint getOrCreateWithAvailableLedgersAndEntries(UUID memberId, LocalDateTime now) {
        return findByMemberIdWithAvailableLedgersAndEntries(memberId, now)
                .orElseGet(() -> MemberPoint.create(memberId));
    }

    // =====================================================
    // 저장 메서드
    // =====================================================

    @Override
    public MemberPoint saveWithEntries(MemberPoint memberPoint) {
        if (memberPoint.ledgers() == null || memberPoint.ledgers().isEmpty()) {
            return memberPoint;
        }

        saveLedgersWithEntries(memberPoint.ledgers());
        return memberPoint;
    }

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

}
