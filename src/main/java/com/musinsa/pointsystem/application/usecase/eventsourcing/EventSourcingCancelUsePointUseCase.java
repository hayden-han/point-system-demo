package com.musinsa.pointsystem.application.usecase.eventsourcing;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.event.PointUseCanceledEvent;
import com.musinsa.pointsystem.domain.event.PointUsedEvent;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.PointEventStore;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 이벤트 소싱 기반 포인트 사용취소 UseCase
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSourcingCancelUsePointUseCase {

    private static final int SNAPSHOT_INTERVAL = 100;

    private final PointEventStore eventStore;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @DistributedLock(key = "'lock:point:member:' + #memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command, UUID memberId) {
        // 1. 원본 사용 이벤트에서 orderId 조회
        PointUsedEvent originalUseEvent = findOriginalUseEvent(memberId, command.getTransactionId());

        // 2. 사용 상세 조회 (기존 State 기반 데이터 활용)
        List<PointUsageDetail> usageDetails = pointUsageDetailRepository
                .findByTransactionId(command.getTransactionId());

        // 3. 이벤트 로드 → Aggregate 복원
        long currentVersion = eventStore.getCurrentVersion(memberId);
        MemberPoint memberPoint = loadAggregate(memberId);

        // 4. 정책 조회
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();

        // 5. 커맨드 처리 → 이벤트 생성
        PointAmount cancelAmount = command.getCancelAmount() != null
                ? PointAmount.of(command.getCancelAmount())
                : calculateTotalCancelableAmount(usageDetails);

        MemberPoint.CancelUseEventResult result = memberPoint.processCancelUse(
                usageDetails,
                cancelAmount,
                expirationPolicy.defaultExpirationDays(),
                command.getTransactionId(),
                originalUseEvent.orderId(),
                currentVersion
        );

        // 6. 이벤트 저장
        eventStore.append(
                memberId,
                List.of(result.event()),
                currentVersion
        );

        log.info("Event sourcing: Canceled use {} points for member {} (original tx: {}, version {})",
                cancelAmount.getValue(), memberId, command.getTransactionId(),
                result.event().getVersion());

        // 7. 사용 상세 업데이트 (State 기반 데이터 동기화)
        pointUsageDetailRepository.saveAll(result.updatedUsageDetails());

        // 8. 스냅샷 저장 (주기적)
        maybeSaveSnapshot(result.memberPoint(), result.event().getVersion());

        // 9. 결과 반환
        PointUseCanceledEvent event = result.event();
        return CancelUsePointResult.builder()
                .transactionId(event.getEventId())
                .memberId(memberId)
                .canceledAmount(event.canceledAmount())
                .totalBalance(result.memberPoint().getTotalBalance().getValue())
                .build();
    }

    private PointUsedEvent findOriginalUseEvent(UUID memberId, UUID transactionId) {
        List<PointEvent> events = eventStore.getEvents(memberId);
        return events.stream()
                .filter(e -> e instanceof PointUsedEvent)
                .map(e -> (PointUsedEvent) e)
                .filter(e -> e.transactionId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Original use event not found: " + transactionId));
    }

    private PointAmount calculateTotalCancelableAmount(List<PointUsageDetail> usageDetails) {
        return usageDetails.stream()
                .map(PointUsageDetail::getCancelableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);
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
