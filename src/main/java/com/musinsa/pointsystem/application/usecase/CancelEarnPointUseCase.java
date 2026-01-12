package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.application.port.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelEarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        // 회원 포인트 조회 (Ledgers 포함)
        MemberPoint memberPoint = memberPointRepository.getByMemberIdWithLedgers(command.getMemberId());

        // Aggregate 메서드 호출 (검증 + 취소 + 잔액 업데이트)
        PointAmount canceledAmount = memberPoint.cancelEarn(command.getLedgerId());

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(memberPoint);

        // 트랜잭션 기록
        PointTransaction transaction = PointTransaction.createEarnCancel(
                command.getMemberId(),
                canceledAmount,
                command.getLedgerId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        return CancelEarnPointResult.builder()
                .ledgerId(command.getLedgerId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .canceledAmount(canceledAmount.getValue())
                .totalBalance(memberPoint.getTotalBalance().getValue())
                .build();
    }
}
