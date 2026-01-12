package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        // DTO → 도메인 타입 변환
        PointAmount amount = PointAmount.of(command.getAmount());
        EarnType earnType = EarnType.valueOf(command.getEarnType());

        // 정책 조회
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();

        // 회원 포인트 조회 (Ledgers 포함)
        MemberPoint memberPoint = memberPointRepository.getOrCreateWithLedgers(command.getMemberId());

        // Aggregate 메서드 호출 (불변 - 새 MemberPoint 반환)
        MemberPoint.EarnResult earnResult = memberPoint.earnWithExpirationValidation(
                amount,
                earnType,
                command.getExpirationDays(),
                policy
        );

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = earnResult.memberPoint();
        PointLedger ledger = earnResult.ledger();

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(updatedMemberPoint);

        // 트랜잭션 기록 (감사 로그)
        PointTransaction transaction = PointTransaction.createEarn(
                command.getMemberId(),
                amount,
                ledger.getId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        return EarnPointResult.builder()
                .ledgerId(ledger.getId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .earnedAmount(amount.getValue())
                .totalBalance(updatedMemberPoint.getTotalBalance().getValue())
                .expiredAt(ledger.getExpiredAt())
                .build();
    }
}
