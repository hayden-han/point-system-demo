package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.domain.event.PointUseCanceledEvent;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.DistributedLock;
import com.musinsa.pointsystem.domain.repository.PointEventPublisher;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.domain.model.PointRules;
import com.musinsa.pointsystem.domain.service.UseCancelProcessor;
import com.musinsa.pointsystem.domain.service.UseCancelProcessor.CancelResult;
import com.musinsa.pointsystem.domain.service.UseCancelProcessor.CancelableContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 포인트 사용취소 UseCase
 * - 오케스트레이션만 담당
 * - 비즈니스 로직은 UseCancelProcessor에 위임
 * - 멱등성 처리는 Controller(IdempotencySupport)에서 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelUsePointUseCase {

    private final PointLedgerRepository pointLedgerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointQueryRepository pointQueryRepository;
    private final PointEventPublisher eventPublisher;
    private final UseCancelProcessor useCancelProcessor;
    private final Clock clock;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command) {
        log.info("포인트 사용취소 시작. memberId={}, orderId={}, cancelAmount={}",
                command.memberId(), command.orderId(), command.cancelAmount());

        LocalDateTime now = LocalDateTime.now(clock);

        // 1. 데이터 조회
        CancelContext context = loadCancelContext(command.orderId());

        // 2. 취소 가능 금액 계산 및 검증
        CancelableContext cancelable = useCancelProcessor.calculateCancelableAmount(
                context.ledgers(), context.entriesByLedgerId(), command.orderId());
        PointRules.validateCancelAmount(command.cancelAmount(), cancelable.totalCancelable());

        // 3. 사용취소 처리 (도메인 로직)
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();
        CancelResult result = useCancelProcessor.process(
                cancelable.cancelableInfos(),
                command.cancelAmount(),
                command.memberId(),
                command.orderId(),
                expirationPolicy.defaultExpirationDays(),
                now
        );

        // 4. 저장
        saveResult(result);

        // 5. 이벤트 발행 (캐시 무효화는 이벤트 핸들러에서 트랜잭션 커밋 후 처리)
        eventPublisher.publish(PointUseCanceledEvent.of(
                command.memberId(),
                command.cancelAmount(),
                command.orderId(),
                result.newLedgers().size(),
                now
        ));

        // 7. 결과 반환
        long totalBalance = pointQueryRepository.getTotalBalance(command.memberId(), now).getValue();
        log.info("포인트 사용취소 완료. memberId={}, canceledAmount={}, totalBalance={}, newLedgerCount={}",
                command.memberId(), command.cancelAmount(), totalBalance, result.newLedgers().size());

        return CancelUsePointResult.builder()
                .memberId(command.memberId())
                .canceledAmount(command.cancelAmount())
                .totalBalance(totalBalance)
                .orderId(command.orderId())
                .build();
    }

    private record CancelContext(
            List<PointLedger> ledgers,
            Map<UUID, List<LedgerEntry>> entriesByLedgerId
    ) {}

    private CancelContext loadCancelContext(String orderId) {
        List<UUID> ledgerIds = ledgerEntryRepository.findLedgerIdsByOrderId(orderId);
        if (ledgerIds.isEmpty()) {
            PointRules.validateCancelAmount(1, 0); // 0으로 검증하여 예외 발생
        }

        List<PointLedger> ledgers = pointLedgerRepository.findAllByIds(ledgerIds);
        List<LedgerEntry> allEntries = ledgerEntryRepository.findByLedgerIds(ledgerIds);
        Map<UUID, List<LedgerEntry>> entriesByLedgerId = allEntries.stream()
                .collect(Collectors.groupingBy(LedgerEntry::ledgerId));

        return new CancelContext(ledgers, entriesByLedgerId);
    }

    private void saveResult(CancelResult result) {
        if (!result.updatedLedgers().isEmpty()) {
            pointLedgerRepository.saveAll(result.updatedLedgers());
        }
        if (!result.newLedgers().isEmpty()) {
            pointLedgerRepository.saveAll(result.newLedgers());
        }
        if (!result.newEntries().isEmpty()) {
            ledgerEntryRepository.saveAll(result.newEntries());
        }
    }
}
