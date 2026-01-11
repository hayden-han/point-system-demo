package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.service.PointEarnValidator;
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
        // DTO → 도메인 타입 변환
        PointAmount amount = PointAmount.of(command.getAmount());

        // 정책 조회 (1회 쿼리)
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();

        // 회원 포인트 조회
        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());

        // 유효성 검증 (도메인 서비스)
        pointEarnValidator.validate(
                amount,
                command.getExpirationDays(),
                memberPoint,
                policy
        );

        // 만료일 계산 (도메인 로직을 EarnPolicyConfig로 이동)
        LocalDateTime expiredAt = policy.calculateExpirationDate(command.getExpirationDays());

        // 적립건 생성
        PointLedger pointLedger = PointLedger.create(
                command.getMemberId(),
                amount,
                command.getEarnType(),
                expiredAt
        );
        PointLedger savedLedger = pointLedgerRepository.save(pointLedger);

        // 트랜잭션 기록
        PointTransaction transaction = PointTransaction.createEarn(
                command.getMemberId(),
                amount,
                savedLedger.getId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        // 잔액 업데이트
        memberPoint.increaseBalance(amount);
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return EarnPointResult.builder()
                .ledgerId(savedLedger.getId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .earnedAmount(amount.getValue())
                .totalBalance(savedMemberPoint.getTotalBalance().getValue())
                .expiredAt(expiredAt)
                .build();
    }
}
