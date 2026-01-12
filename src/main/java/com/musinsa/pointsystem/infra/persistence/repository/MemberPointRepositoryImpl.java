package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.MemberPointMapper;
import com.musinsa.pointsystem.infra.persistence.mapper.PointLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final MemberPointJpaRepository jpaRepository;
    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final MemberPointMapper mapper;
    private final PointLedgerMapper pointLedgerMapper;

    @Override
    public Optional<MemberPoint> findByMemberId(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithLedgers(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findByMemberId(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgerEntities);
                });
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgers(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findAvailableByMemberId(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgerEntities);
                });
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        // MemberPoint 저장/업데이트
        MemberPointEntity memberPointEntity = jpaRepository.findById(memberPoint.getMemberId())
                .map(existing -> {
                    existing.updateTotalBalance(memberPoint.getTotalBalance().getValue());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(memberPoint));

        MemberPointEntity savedMemberPointEntity = jpaRepository.save(memberPointEntity);

        // Ledgers 저장
        if (memberPoint.getLedgers() != null && !memberPoint.getLedgers().isEmpty()) {
            saveLedgers(memberPoint.getLedgers());
        }

        return mapper.toDomain(savedMemberPointEntity);
    }

    /**
     * Ledgers 배치 저장 (신규/기존 구분)
     */
    private void saveLedgers(List<PointLedger> ledgers) {
        if (ledgers.isEmpty()) {
            return;
        }

        List<UUID> ids = ledgers.stream()
                .map(PointLedger::getId)
                .toList();

        // 기존 엔티티 조회
        Map<UUID, PointLedgerEntity> existingEntityMap = pointLedgerJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

        List<PointLedgerEntity> newEntities = new ArrayList<>();

        for (PointLedger ledger : ledgers) {
            PointLedgerEntity existingEntity = existingEntityMap.get(ledger.getId());
            if (existingEntity != null) {
                // 기존 엔티티 업데이트
                existingEntity.updateAvailableAmount(
                        ledger.getAvailableAmount().getValue(),
                        ledger.getUsedAmount().getValue()
                );
                if (ledger.isCanceled()) {
                    existingEntity.cancel();
                }
            } else {
                // 신규 엔티티
                newEntities.add(pointLedgerMapper.toEntity(ledger));
            }
        }

        // 신규 엔티티 배치 저장
        if (!newEntities.isEmpty()) {
            pointLedgerJpaRepository.saveAll(newEntities);
        }

        // 기존 엔티티는 변경감지로 자동 업데이트됨
        if (!existingEntityMap.isEmpty()) {
            pointLedgerJpaRepository.saveAll(existingEntityMap.values().stream().toList());
        }
    }

    @Override
    public MemberPoint getOrCreate(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain)
                .orElseGet(() -> {
                    MemberPoint newMemberPoint = MemberPoint.create(memberId);
                    MemberPointEntity entity = mapper.toEntity(newMemberPoint);
                    return mapper.toDomain(jpaRepository.save(entity));
                });
    }

    @Override
    public MemberPoint getOrCreateWithLedgers(UUID memberId) {
        Optional<MemberPointEntity> entityOpt = jpaRepository.findById(memberId);

        if (entityOpt.isPresent()) {
            List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findByMemberId(memberId);
            return mapper.toDomainWithLedgers(entityOpt.get(), ledgerEntities);
        } else {
            MemberPoint newMemberPoint = MemberPoint.create(memberId);
            MemberPointEntity entity = mapper.toEntity(newMemberPoint);
            MemberPointEntity savedEntity = jpaRepository.save(entity);
            return mapper.toDomainWithLedgers(savedEntity, List.of());
        }
    }

    @Override
    public MemberPoint getOrCreateWithAvailableLedgers(UUID memberId) {
        Optional<MemberPointEntity> entityOpt = jpaRepository.findById(memberId);

        if (entityOpt.isPresent()) {
            List<PointLedgerEntity> ledgerEntities = pointLedgerJpaRepository.findAvailableByMemberId(memberId);
            return mapper.toDomainWithLedgers(entityOpt.get(), ledgerEntities);
        } else {
            MemberPoint newMemberPoint = MemberPoint.create(memberId);
            MemberPointEntity entity = mapper.toEntity(newMemberPoint);
            MemberPointEntity savedEntity = jpaRepository.save(entity);
            return mapper.toDomainWithLedgers(savedEntity, List.of());
        }
    }
}
