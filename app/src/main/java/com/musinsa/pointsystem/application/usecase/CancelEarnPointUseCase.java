package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.domain.event.PointEarnCanceledEvent;
import com.musinsa.pointsystem.domain.exception.PointLedgerNotFoundException;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.DistributedLock;
import com.musinsa.pointsystem.domain.repository.IdGenerator;
import com.musinsa.pointsystem.domain.repository.PointEventPublisher;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.domain.model.PointRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 포인트 적립 취소 UseCase
 * - 오케스트레이션만 담당
 * - 비즈니스 규칙은 PointRules에 위임
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelEarnPointUseCase {

    private final PointLedgerRepository pointLedgerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PointQueryRepository pointQueryRepository;
    private final PointEventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        log.info("포인트 적립취소 시작. memberId={}, ledgerId={}",
                command.memberId(), command.ledgerId());

        LocalDateTime now = LocalDateTime.now(clock);

        // 1. Ledger 조회
        PointLedger ledger = pointLedgerRepository.findById(command.ledgerId())
                .filter(l -> l.memberId().equals(command.memberId()))
                .orElseThrow(() -> new PointLedgerNotFoundException(command.ledgerId()));

        // 2. 취소 가능 여부 검증 (PointRules에 위임)
        PointRules.validateCancelEarn(ledger);

        // 3. Ledger 취소 처리
        PointLedger canceledLedger = ledger.withCanceled();
        pointLedgerRepository.save(canceledLedger);

        // 4. EARN_CANCEL Entry 생성 및 저장
        LedgerEntry cancelEntry = LedgerEntry.createEarnCancel(
                idGenerator.generate(),
                ledger.id(),
                ledger.earnedAmount(),
                now
        );
        ledgerEntryRepository.save(cancelEntry);

        // 5. 이벤트 발행
        eventPublisher.publish(PointEarnCanceledEvent.of(
                command.memberId(),
                command.ledgerId(),
                ledger.earnedAmount(),
                now
        ));

        // 6. 잔액 조회
        long totalBalance = pointQueryRepository.getTotalBalance(command.memberId(), now).getValue();

        log.info("포인트 적립취소 완료. memberId={}, ledgerId={}, canceledAmount={}, totalBalance={}",
                command.memberId(), command.ledgerId(), ledger.earnedAmount(), totalBalance);

        return CancelEarnPointResult.builder()
                .ledgerId(command.ledgerId())
                .memberId(command.memberId())
                .canceledAmount(ledger.earnedAmount())
                .totalBalance(totalBalance)
                .build();
    }
}
