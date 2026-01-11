package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.domain.exception.PointLedgerNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.application.port.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelEarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        PointLedger pointLedger = pointLedgerRepository.findById(command.getLedgerId())
                .orElseThrow(() -> new PointLedgerNotFoundException(command.getLedgerId()));

        if (pointLedger.isCanceled()) {
            throw new PointLedgerAlreadyCanceledException(command.getLedgerId());
        }

        if (!pointLedger.canCancel()) {
            throw new PointLedgerAlreadyUsedException(command.getLedgerId());
        }

        PointAmount canceledAmount = pointLedger.getEarnedAmount();
        pointLedger.cancel();
        pointLedgerRepository.save(pointLedger);

        PointTransaction transaction = PointTransaction.createEarnCancel(
                command.getMemberId(),
                canceledAmount,
                command.getLedgerId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());
        memberPoint.decreaseBalance(canceledAmount);
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return CancelEarnPointResult.builder()
                .ledgerId(command.getLedgerId())
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .canceledAmount(canceledAmount.getValue())
                .totalBalance(savedMemberPoint.getTotalBalance().getValue())
                .build();
    }
}
