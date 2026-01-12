package com.musinsa.pointsystem.application.usecase.eventsourcing;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.event.PointUsedEvent;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.PointEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 이벤트 소싱 기반 포인트 사용 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSourcingUsePointUseCase {

    private static final int SNAPSHOT_INTERVAL = 100;

    private final PointEventStore eventStore;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        // 1. 이벤트 로드 → Aggregate 복원
        long currentVersion = eventStore.getCurrentVersion(command.getMemberId());
        MemberPoint memberPoint = loadAggregate(command.getMemberId());

        // 2. 커맨드 처리 → 이벤트 생성
        MemberPoint.UseEventResult result = memberPoint.processUse(
                PointAmount.of(command.getAmount()),
                command.getOrderId(),
                currentVersion
        );

        // 3. 이벤트 저장
        eventStore.append(
                command.getMemberId(),
                List.of(result.event()),
                currentVersion
        );

        log.info("Event sourcing: Used {} points for member {} (order: {}, version {})",
                command.getAmount(), command.getMemberId(), command.getOrderId(),
                result.event().getVersion());

        // 4. 스냅샷 저장 (주기적)
        maybeSaveSnapshot(result.memberPoint(), result.event().getVersion());

        // 5. 결과 반환
        PointUsedEvent event = result.event();
        return UsePointResult.builder()
                .transactionId(event.transactionId())
                .memberId(command.getMemberId())
                .usedAmount(event.amount())
                .totalBalance(result.memberPoint().getTotalBalance().getValue())
                .orderId(command.getOrderId())
                .build();
    }

    private MemberPoint loadAggregate(UUID memberId) {
        Optional<MemberPointSnapshot> snapshot = eventStore.getLatestSnapshot(memberId);

        if (snapshot.isPresent()) {
            List<PointEvent> events = eventStore.getEvents(memberId, snapshot.get().version());
            return MemberPoint.reconstitute(snapshot.get(), events);
        } else {
            List<PointEvent> events = eventStore.getEvents(memberId);
            if (events.isEmpty()) {
                return MemberPoint.create(memberId);
            }
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
