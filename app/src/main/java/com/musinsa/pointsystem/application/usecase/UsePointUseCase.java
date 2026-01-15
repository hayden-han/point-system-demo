package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.domain.event.PointUsedEvent;
import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.DistributedLock;
import com.musinsa.pointsystem.domain.repository.IdGenerator;
import com.musinsa.pointsystem.domain.repository.PointEventPublisher;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.model.PointRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포인트 사용 UseCase
 * - 오케스트레이션만 담당
 * - 비즈니스 규칙은 PointRules에 위임
 * - 멱등성 처리는 Controller(IdempotencySupport)에서 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsePointUseCase {

    private final PointLedgerRepository pointLedgerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PointEventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        log.info("포인트 사용 시작. memberId={}, amount={}, orderId={}",
                command.memberId(), command.amount(), command.orderId());

        LocalDateTime now = LocalDateTime.now(clock);

        // 1. 주문 ID 검증
        if (command.orderId() == null || command.orderId().isBlank()) {
            throw new InvalidOrderIdException("주문 ID는 필수입니다.");
        }

        // 2. 사용 가능한 Ledger 조회
        List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(
                command.memberId(), now);

        // 3. 잔액 계산 및 검증
        long availableBalance = PointRules.calculateAvailableBalance(availableLedgers, now);
        PointRules.validateSufficientBalance(availableBalance, command.amount());

        // 4. 선입선출 차감 처리
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<LedgerEntry> newEntries = new ArrayList<>();
        long remainingAmount = command.amount();

        List<PointLedger> sortedLedgers = PointRules.getAvailableLedgersSorted(availableLedgers, now);

        for (PointLedger ledger : sortedLedgers) {
            if (remainingAmount <= 0) break;

            long useAmount = Math.min(remainingAmount, ledger.availableAmount());
            remainingAmount -= useAmount;

            // Ledger 업데이트
            PointLedger updated = ledger.withAvailableAmount(ledger.availableAmount() - useAmount);
            updatedLedgers.add(updated);

            // USE Entry 생성
            LedgerEntry useEntry = LedgerEntry.createUse(
                    idGenerator.generate(),
                    ledger.id(),
                    useAmount,
                    command.orderId(),
                    now
            );
            newEntries.add(useEntry);
        }

        // 5. 저장
        pointLedgerRepository.saveAll(updatedLedgers);
        ledgerEntryRepository.saveAll(newEntries);

        // 6. 이벤트 발행 (캐시 무효화는 이벤트 핸들러에서 트랜잭션 커밋 후 처리)
        eventPublisher.publish(PointUsedEvent.of(
                command.memberId(),
                command.amount(),
                command.orderId(),
                updatedLedgers.size(),
                now
        ));

        long newBalance = availableBalance - command.amount();

        log.info("포인트 사용 완료. memberId={}, usedAmount={}, totalBalance={}, usedLedgerCount={}",
                command.memberId(), command.amount(), newBalance, updatedLedgers.size());

        return UsePointResult.builder()
                .memberId(command.memberId())
                .usedAmount(command.amount())
                .totalBalance(newBalance)
                .orderId(command.orderId())
                .build();
    }
}
