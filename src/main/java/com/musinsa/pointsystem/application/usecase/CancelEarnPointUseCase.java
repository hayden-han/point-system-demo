package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.service.PointAccrualManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelEarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointAccrualManager pointAccrualManager;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        // 회원 포인트 조회 (Ledgers 포함)
        MemberPoint memberPoint = memberPointRepository.findByMemberIdWithLedgers(command.memberId())
                .orElseThrow(() -> new MemberPointNotFoundException(command.memberId()));

        // Domain Service를 통한 적립 취소 처리
        MemberPoint.CancelEarnResult cancelResult = pointAccrualManager.cancelEarn(memberPoint, command.ledgerId());

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = cancelResult.memberPoint();
        PointAmount canceledAmount = cancelResult.canceledAmount();

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(updatedMemberPoint);

        // 트랜잭션 기록
        PointTransaction transaction = pointAccrualManager.createEarnCancelTransaction(
                command.memberId(),
                canceledAmount,
                command.ledgerId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        return CancelEarnPointResult.builder()
                .ledgerId(command.ledgerId())
                .transactionId(savedTransaction.id())
                .memberId(command.memberId())
                .canceledAmount(canceledAmount.getValue())
                .totalBalance(updatedMemberPoint.totalBalance().getValue())
                .build();
    }
}
