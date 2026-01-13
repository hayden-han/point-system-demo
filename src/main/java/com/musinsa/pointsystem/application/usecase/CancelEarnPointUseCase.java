package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.service.PointAccrualManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelEarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointAccrualManager pointAccrualManager;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        log.info("포인트 적립취소 시작. memberId={}, ledgerId={}",
                command.memberId(), command.ledgerId());

        LocalDateTime now = timeProvider.now();

        // 회원 포인트 조회 (v2: Entry 포함)
        MemberPoint memberPoint = memberPointRepository.getByMemberIdWithAllLedgersAndEntries(command.memberId());

        // Domain Service를 통한 적립 취소 처리 (v2)
        MemberPoint.CancelEarnResult cancelResult = pointAccrualManager.cancelEarnV2(memberPoint, command.ledgerId());

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = cancelResult.memberPoint();
        PointAmount canceledAmount = cancelResult.canceledAmount();

        // MemberPoint + Ledgers + Entries 저장 (v2)
        memberPointRepository.saveWithEntries(updatedMemberPoint);

        // 트랜잭션 기록 (레거시 호환)
        PointTransaction transaction = pointAccrualManager.createEarnCancelTransaction(
                command.memberId(),
                canceledAmount,
                command.ledgerId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        log.info("포인트 적립취소 완료. memberId={}, ledgerId={}, transactionId={}, canceledAmount={}, totalBalance={}",
                command.memberId(), command.ledgerId(), savedTransaction.id(),
                canceledAmount.getValue(), updatedMemberPoint.getTotalBalance(now).getValue());

        return CancelEarnPointResult.builder()
                .ledgerId(command.ledgerId())
                .transactionId(savedTransaction.id())
                .memberId(command.memberId())
                .canceledAmount(canceledAmount.getValue())
                .totalBalance(updatedMemberPoint.getTotalBalance(now).getValue())
                .build();
    }
}
