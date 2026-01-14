package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 포인트 적립 UseCase
 *
 * <p>최적화: 적립 시에는 기존 Ledger를 수정하지 않으므로,
 * Aggregate 전체 로드 없이 잔액만 Query로 조회하여 검증.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EarnPointUseCase {

    private final PointQueryRepository pointQueryRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointFactory pointFactory;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        log.info("포인트 적립 시작. memberId={}, amount={}, earnType={}",
                command.memberId(), command.amount(), command.earnType());

        LocalDateTime now = timeProvider.now();

        // DTO → 도메인 타입 변환
        PointAmount amount = PointAmount.of(command.amount());
        EarnType earnType = EarnType.valueOf(command.earnType());

        // 정책 조회
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();

        // 최적화: Aggregate 로드 없이 현재 잔액만 조회
        PointAmount currentBalance = pointQueryRepository.getTotalBalance(command.memberId(), now);

        // 도메인 검증 (MemberPoint의 static 메서드)
        MemberPoint.validateEarnWithBalance(amount, currentBalance, command.expirationDays(), policy);

        // 만료일 계산
        LocalDateTime expiredAt = policy.calculateExpirationDate(command.expirationDays());

        // Ledger 생성 (EARN Entry 자동 포함)
        PointLedger ledger = pointFactory.createLedger(
                command.memberId(),
                amount,
                earnType,
                expiredAt
        );

        // 신규 Ledger 저장 (기존 Ledger 수정 없음)
        pointLedgerRepository.save(ledger);

        PointAmount newTotalBalance = currentBalance.add(amount);

        log.info("포인트 적립 완료. memberId={}, ledgerId={}, earnedAmount={}, totalBalance={}",
                command.memberId(), ledger.id(), amount.getValue(), newTotalBalance.getValue());

        return EarnPointResult.builder()
                .ledgerId(ledger.id())
                .memberId(command.memberId())
                .earnedAmount(amount.getValue())
                .totalBalance(newTotalBalance.getValue())
                .expiredAt(ledger.expiredAt())
                .build();
    }
}
