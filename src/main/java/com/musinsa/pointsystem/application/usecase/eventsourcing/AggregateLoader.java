package com.musinsa.pointsystem.application.usecase.eventsourcing;

import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.MemberPointSnapshot;
import com.musinsa.pointsystem.domain.repository.PointEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate 로딩 헬퍼
 * - 스냅샷 + 이벤트 기반 Aggregate 복원
 * - 스냅샷 저장 로직 공통화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregateLoader {

    private static final int SNAPSHOT_INTERVAL = 100;

    private final PointEventStore eventStore;

    /**
     * Aggregate 로드 (스냅샷 + 이벤트 리플레이)
     */
    public MemberPoint load(UUID memberId) {
        Optional<MemberPointSnapshot> snapshot = eventStore.getLatestSnapshot(memberId);

        if (snapshot.isPresent()) {
            List<PointEvent> events = eventStore.getEvents(memberId, snapshot.get().version());
            log.debug("Loading aggregate {} from snapshot (version {}) + {} events",
                    memberId, snapshot.get().version(), events.size());
            return MemberPoint.reconstitute(snapshot.get(), events);
        } else {
            List<PointEvent> events = eventStore.getEvents(memberId);
            if (events.isEmpty()) {
                log.debug("Creating new aggregate for member {}", memberId);
                return MemberPoint.create(memberId);
            }
            log.debug("Loading aggregate {} from {} events", memberId, events.size());
            return MemberPoint.reconstitute(memberId, events);
        }
    }

    /**
     * 현재 버전 조회
     */
    public long getCurrentVersion(UUID memberId) {
        return eventStore.getCurrentVersion(memberId);
    }

    /**
     * 스냅샷 저장 (조건부)
     */
    public void maybeSaveSnapshot(MemberPoint memberPoint, long version) {
        if (version > 0 && version % SNAPSHOT_INTERVAL == 0) {
            MemberPointSnapshot snapshot = memberPoint.toSnapshot(version);
            eventStore.saveSnapshot(snapshot);
            log.info("Saved snapshot for member {} at version {}", memberPoint.memberId(), version);
        }
    }

    /**
     * 스냅샷 강제 저장
     */
    public void saveSnapshot(MemberPoint memberPoint, long version) {
        MemberPointSnapshot snapshot = memberPoint.toSnapshot(version);
        eventStore.saveSnapshot(snapshot);
        log.info("Saved snapshot for member {} at version {}", memberPoint.memberId(), version);
    }
}
