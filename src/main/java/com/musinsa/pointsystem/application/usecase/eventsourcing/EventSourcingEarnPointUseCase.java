package com.musinsa.pointsystem.application.usecase.eventsourcing;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.event.PointEarnedEvent;
import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.PointEventStore;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 이벤트 소싱 기반 포인트 적립 UseCase
 * - Event Store에서 이벤트 로드 → Aggregate 복원
 * - 커맨드 처리 → 이벤트 생성
 * - 이벤트 저장 (낙관적 동시성 제어)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSourcingEarnPointUseCase {

    private static final int SNAPSHOT_INTERVAL = 100;

    private final PointEventStore eventStore;
    private final PointPolicyRepository pointPolicyRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        // 1. 이벤트 로드 → Aggregate 복원
        long currentVersion = eventStore.getCurrentVersion(command.getMemberId());
        MemberPoint memberPoint = loadAggregate(command.getMemberId());

        // 2. 정책 조회
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();
        java.time.LocalDateTime expiredAt = policy.calculateExpirationDate(command.getExpirationDays());

        // 3. 커맨드 처리 → 이벤트 생성
        MemberPoint.EarnEventResult result = memberPoint.processEarn(
                PointAmount.of(command.getAmount()),
                EarnType.valueOf(command.getEarnType()),
                expiredAt,
                policy,
                currentVersion
        );

        // 4. 이벤트 저장
        eventStore.append(
                command.getMemberId(),
                List.of(result.event()),
                currentVersion
        );

        log.info("Event sourcing: Earned {} points for member {} (version {})",
                command.getAmount(), command.getMemberId(), result.event().getVersion());

        // 5. 스냅샷 저장 (주기적)
        maybeSaveSnapshot(result.memberPoint(), result.event().getVersion());

        // 6. 결과 반환
        PointEarnedEvent event = result.event();
        return EarnPointResult.builder()
                .ledgerId(event.ledgerId())
                .transactionId(event.getEventId())  // 이벤트 ID를 트랜잭션 ID로 사용
                .memberId(command.getMemberId())
                .earnedAmount(event.amount())
                .totalBalance(result.memberPoint().getTotalBalance().getValue())
                .expiredAt(event.expiredAt())
                .build();
    }

    private MemberPoint loadAggregate(UUID memberId) {
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

    private void maybeSaveSnapshot(MemberPoint memberPoint, long version) {
        if (version > 0 && version % SNAPSHOT_INTERVAL == 0) {
            MemberPointSnapshot snapshot = memberPoint.toSnapshot(version);
            eventStore.saveSnapshot(snapshot);
            log.info("Saved snapshot for member {} at version {}", memberPoint.memberId(), version);
        }
    }
}
