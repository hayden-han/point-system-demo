package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.model.MemberPointSnapshot;
import com.musinsa.pointsystem.domain.repository.PointEventStore;
import com.musinsa.pointsystem.infra.persistence.entity.PointEventEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointSnapshotEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointEventMapper;
import com.musinsa.pointsystem.infra.persistence.mapper.PointSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 포인트 이벤트 저장소 구현체
 * - 이벤트 소싱의 핵심 인프라
 * - 낙관적 동시성 제어 (aggregate_id + version 유니크)
 * - 스냅샷 기반 성능 최적화
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PointEventStoreImpl implements PointEventStore {

    private final PointEventJpaRepository eventRepository;
    private final PointSnapshotJpaRepository snapshotRepository;
    private final PointEventMapper eventMapper;
    private final PointSnapshotMapper snapshotMapper;

    @Override
    @Transactional
    public void append(UUID aggregateId, List<PointEvent> events, long expectedVersion) {
        // 낙관적 동시성 제어: 버전 충돌 체크
        long currentVersion = getCurrentVersion(aggregateId);

        if (currentVersion != expectedVersion) {
            log.warn("Concurrency conflict for aggregate {}: expected {}, actual {}",
                    aggregateId, expectedVersion, currentVersion);
            throw new ConcurrencyConflictException(aggregateId, expectedVersion, currentVersion);
        }

        // 이벤트 저장
        List<PointEventEntity> entities = events.stream()
                .map(eventMapper::toEntity)
                .toList();

        eventRepository.saveAll(entities);

        log.debug("Appended {} events for aggregate {} (version {} -> {})",
                events.size(), aggregateId, expectedVersion, expectedVersion + events.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointEvent> getEvents(UUID aggregateId) {
        List<PointEventEntity> entities = eventRepository
                .findByAggregateIdOrderByVersionAsc(aggregateId);

        return entities.stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointEvent> getEvents(UUID aggregateId, long fromVersion) {
        List<PointEventEntity> entities = eventRepository
                .findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId, fromVersion);

        return entities.stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberPointSnapshot> getLatestSnapshot(UUID aggregateId) {
        return snapshotRepository.findByAggregateId(aggregateId)
                .map(snapshotMapper::toDomain);
    }

    @Override
    @Transactional
    public void saveSnapshot(MemberPointSnapshot snapshot) {
        Optional<PointSnapshotEntity> existing = snapshotRepository
                .findByAggregateId(snapshot.memberId());

        if (existing.isPresent()) {
            // 기존 스냅샷 업데이트
            PointSnapshotEntity entity = existing.get();
            PointSnapshotEntity newEntity = snapshotMapper.toEntity(snapshot);
            entity.update(newEntity.getSnapshotData(), newEntity.getVersion(), newEntity.getCreatedAt());
            log.debug("Updated snapshot for aggregate {} at version {}",
                    snapshot.memberId(), snapshot.version());
        } else {
            // 새 스냅샷 생성
            PointSnapshotEntity entity = snapshotMapper.toEntity(snapshot);
            snapshotRepository.save(entity);
            log.debug("Created snapshot for aggregate {} at version {}",
                    snapshot.memberId(), snapshot.version());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getCurrentVersion(UUID aggregateId) {
        return eventRepository.findMaxVersionByAggregateId(aggregateId)
                .orElse(0L);
    }
}
