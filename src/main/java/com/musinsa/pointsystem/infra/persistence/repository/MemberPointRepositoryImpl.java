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

    /**
     * MemberPoint와 모든 Ledgers를 별도 쿼리로 조회
     * - JOIN FETCH 대신 별도 쿼리 사용으로 N+1 문제 방지
     * - DB에서 정렬 완료
     */
    @Override
    public Optional<MemberPoint> findByMemberIdWithAllLedgers(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgers = pointLedgerJpaRepository
                            .findByMemberIdOrderByEarnedAtDesc(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgers);
                });
    }

    /**
     * MemberPoint와 사용 가능한 Ledgers만 별도 쿼리로 조회
     * - DB에서 필터링 및 우선순위 정렬 완료
     * - 메모리에서 추가 정렬 불필요
     */
    @Override
    public Optional<MemberPoint> findByMemberIdWithAvailableLedgersForUse(UUID memberId) {
        return jpaRepository.findById(memberId)
                .map(entity -> {
                    List<PointLedgerEntity> ledgers = pointLedgerJpaRepository
                            .findAvailableByMemberIdOrderByPriority(memberId);
                    return mapper.toDomainWithLedgers(entity, ledgers);
                });
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
     * - 기존 엔티티는 Dirty Checking으로 자동 업데이트
     * - 신규 엔티티만 saveAll로 배치 저장
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
                // 기존 엔티티 업데이트 (Dirty Checking)
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
