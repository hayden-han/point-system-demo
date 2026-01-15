package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.domain.event.PointEarnedEvent;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.DistributedLock;
import com.musinsa.pointsystem.domain.repository.IdGenerator;
import com.musinsa.pointsystem.domain.repository.PointEventPublisher;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.domain.model.PointRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 포인트 적립 UseCase
 * - 오케스트레이션만 담당
 * - 비즈니스 규칙은 PointRules에 위임
 * - 멱등성 처리는 Controller(IdempotencySupport)에서 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EarnPointUseCase {

    private final PointQueryRepository pointQueryRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointEventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        log.info("포인트 적립 시작. memberId={}, amount={}, earnType={}",
                command.memberId(), command.amount(), command.earnType());

        LocalDateTime now = LocalDateTime.now(clock);

        // 1. 정책 조회
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();

        // 2. 현재 잔액 조회
        long currentBalance = pointQueryRepository.getTotalBalance(command.memberId(), now).getValue();

        // 3. 비즈니스 규칙 검증
        PointRules.validateEarn(command.amount(), currentBalance, command.expirationDays(), policy);

        // 4. 만료일 계산
        LocalDateTime expiredAt = policy.calculateExpirationDate(command.expirationDays(), now);

        // 5. Ledger 생성 및 저장
        PointLedger ledger = PointLedger.create(
                idGenerator.generate(),
                command.memberId(),
                command.amount(),
                EarnType.valueOf(command.earnType()),
                expiredAt,
                null,
                now
        );
        pointLedgerRepository.save(ledger);

        // 6. EARN Entry 생성 및 저장
        LedgerEntry earnEntry = LedgerEntry.createEarn(
                idGenerator.generate(),
                ledger.id(),
                command.amount(),
                now
        );
        ledgerEntryRepository.save(earnEntry);

        // 7. 이벤트 발행 (캐시 무효화는 이벤트 핸들러에서 트랜잭션 커밋 후 처리)
        eventPublisher.publish(PointEarnedEvent.of(
                command.memberId(),
                ledger.id(),
                command.amount(),
                command.earnType(),
                ledger.expiredAt(),
                now
        ));

        long newTotalBalance = currentBalance + command.amount();

        log.info("포인트 적립 완료. memberId={}, ledgerId={}, earnedAmount={}, totalBalance={}",
                command.memberId(), ledger.id(), command.amount(), newTotalBalance);

        return EarnPointResult.builder()
                .ledgerId(ledger.id())
                .memberId(command.memberId())
                .earnedAmount(command.amount())
                .totalBalance(newTotalBalance)
                .expiredAt(ledger.expiredAt())
                .build();
    }
}
