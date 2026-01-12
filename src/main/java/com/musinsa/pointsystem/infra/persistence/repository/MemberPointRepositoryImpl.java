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
        return jpaRepository.findByIdWithLedgers(memberId)
                .map(mapper::toDomainWithLedgers);
    }

    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgers(UUID memberId) {
        return jpaRepository.findByIdWithAvailableLedgers(memberId)
                .map(mapper::toDomainWithLedgers);
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        // MemberPoint 저장/업데이트
        MemberPointEntity memberPointEntity = jpaRepository.findById(memberPoint.memberId())
                .map(existing -> {
                    existing.updateTotalBalance(memberPoint.totalBalance().value());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(memberPoint));

        MemberPointEntity savedMemberPointEntity = jpaRepository.save(memberPointEntity);

        // Ledgers 저장
        if (memberPoint.ledgers() != null && !memberPoint.ledgers().isEmpty()) {
            saveLedgers(memberPoint.ledgers());
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
                .map(PointLedger::id)
                .toList();

        // 기존 엔티티 조회
        Map<UUID, PointLedgerEntity> existingEntityMap = pointLedgerJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointLedgerEntity::getId, entity -> entity));

        List<PointLedgerEntity> newEntities = new ArrayList<>();

        for (PointLedger ledger : ledgers) {
            PointLedgerEntity existingEntity = existingEntityMap.get(ledger.id());
            if (existingEntity != null) {
                // 기존 엔티티 업데이트
                existingEntity.updateAvailableAmount(
                        ledger.availableAmount().value(),
                        ledger.usedAmount().value()
                );
                if (ledger.canceled()) {
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

        // 기존 엔티티는 변경감지(Dirty Checking)로 자동 업데이트됨
        // 명시적 saveAll 호출 불필요
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
        return jpaRepository.findByIdWithLedgers(memberId)
                .map(mapper::toDomainWithLedgers)
                .orElseGet(() -> createNewMemberPoint(memberId));
    }

    @Override
    public MemberPoint getOrCreateWithAvailableLedgers(UUID memberId) {
        return jpaRepository.findByIdWithAvailableLedgers(memberId)
                .map(mapper::toDomainWithLedgers)
                .orElseGet(() -> createNewMemberPoint(memberId));
    }

    private MemberPoint createNewMemberPoint(UUID memberId) {
        MemberPoint newMemberPoint = MemberPoint.create(memberId);
        MemberPointEntity entity = mapper.toEntity(newMemberPoint);
        MemberPointEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomainWithLedgers(savedEntity);
    }
}
