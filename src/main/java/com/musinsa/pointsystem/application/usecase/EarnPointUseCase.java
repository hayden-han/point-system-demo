package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EarnPointUseCase {

    private final MemberPointService memberPointService;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        // DTO → 도메인 타입 변환
        PointAmount amount = PointAmount.of(command.getAmount());

        // 정책 조회
        EarnPolicyConfig policy = memberPointService.getEarnPolicy();

        // 회원 포인트 조회 (Ledgers 포함)
        MemberPoint memberPoint = memberPointService.getOrCreateMemberPointWithLedgers(command.getMemberId());

        // Aggregate 메서드 호출 (검증 + 적립 + 잔액 업데이트)
        PointLedger ledger = memberPoint.earnWithExpirationValidation(
                amount,
                command.getEarnType(),
                command.getExpirationDays(),
                policy
        );

        // MemberPoint와 Ledgers 함께 저장
        memberPointService.saveMemberPoint(memberPoint);

        // 트랜잭션 기록 (감사 로그)
        PointTransaction transaction = PointTransaction.createEarn(
                command.getMemberId(),
                amount,
                ledger.getId()
        );
        PointTransaction savedTransaction = memberPointService.saveTransaction(transaction);

        return EarnPointResult.builder()
                .ledgerId(ledger.getId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .earnedAmount(amount.getValue())
                .totalBalance(memberPoint.getTotalBalance().getValue())
                .expiredAt(ledger.getExpiredAt())
                .build();
    }
}
