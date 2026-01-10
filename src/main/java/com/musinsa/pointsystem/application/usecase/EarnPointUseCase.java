package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointPolicy;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.service.PointEarnValidator;
import com.musinsa.pointsystem.infra.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EarnPointUseCase {

    private final PointPolicyRepository pointPolicyRepository;
    private final MemberPointRepository memberPointRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointEarnValidator pointEarnValidator;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        Long minAmount = pointPolicyRepository.getValueByKey(PointPolicy.EARN_MIN_AMOUNT);
        Long maxAmount = pointPolicyRepository.getValueByKey(PointPolicy.EARN_MAX_AMOUNT);
        Long maxBalance = pointPolicyRepository.getValueByKey(PointPolicy.BALANCE_MAX_AMOUNT);
        Long defaultDays = pointPolicyRepository.getValueByKey(PointPolicy.EXPIRATION_DEFAULT_DAYS);
        Long minDays = pointPolicyRepository.getValueByKey(PointPolicy.EXPIRATION_MIN_DAYS);
        Long maxDays = pointPolicyRepository.getValueByKey(PointPolicy.EXPIRATION_MAX_DAYS);

        pointEarnValidator.validateAmount(command.getAmount(), minAmount, maxAmount);
        pointEarnValidator.validateExpirationDays(command.getExpirationDays(), minDays, maxDays);

        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());
        pointEarnValidator.validateMaxBalance(memberPoint, command.getAmount(), maxBalance);

        int expirationDays = command.getExpirationDays() != null
                ? command.getExpirationDays()
                : defaultDays.intValue();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(expirationDays);

        PointLedger pointLedger = PointLedger.create(
                command.getMemberId(),
                command.getAmount(),
                command.getEarnType(),
                expiredAt
        );
        PointLedger savedLedger = pointLedgerRepository.save(pointLedger);

        PointTransaction transaction = PointTransaction.createEarn(
                command.getMemberId(),
                command.getAmount(),
                savedLedger.getId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        memberPoint.increaseBalance(command.getAmount());
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return EarnPointResult.builder()
                .ledgerId(savedLedger.getId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .earnedAmount(command.getAmount())
                .totalBalance(savedMemberPoint.getTotalBalance())
                .expiredAt(expiredAt)
                .build();
    }
}
