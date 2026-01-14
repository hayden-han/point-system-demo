package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.exception.PointLedgerNotFoundException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.domain.service.PointAccrualManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 포인트 적립 취소 UseCase
 *
 * <p>최적화: 취소 대상 Ledger만 로드하여 처리.
 * 전체 Ledger를 로드하지 않고 특정 Ledger만 조회.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelEarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointQueryRepository pointQueryRepository;
    private final PointAccrualManager pointAccrualManager;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelEarnPointResult execute(CancelEarnPointCommand command) {
        log.info("포인트 적립취소 시작. memberId={}, ledgerId={}",
                command.memberId(), command.ledgerId());

        LocalDateTime now = timeProvider.now();

        // 최적화: 취소 대상 Ledger만 로드 (전체 Ledger 로드 대신)
        MemberPoint memberPoint = memberPointRepository
                .findByMemberIdWithSpecificLedger(command.memberId(), command.ledgerId())
                .orElseThrow(() -> new PointLedgerNotFoundException(command.ledgerId()));

        // Domain Service를 통한 적립 취소 처리 (EARN_CANCEL Entry 자동 생성)
        MemberPoint.CancelEarnResult cancelResult = pointAccrualManager.cancelEarnV2(memberPoint, command.ledgerId());

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = cancelResult.memberPoint();
        PointAmount canceledAmount = cancelResult.canceledAmount();

        // 변경된 Ledger + Entry 저장
        memberPointRepository.saveWithEntries(updatedMemberPoint);

        // 최적화: 잔액은 별도 쿼리로 조회 (Ledger 1개만 가진 Aggregate에서는 전체 잔액 계산 불가)
        PointAmount totalBalance = pointQueryRepository.getTotalBalance(command.memberId(), now);

        log.info("포인트 적립취소 완료. memberId={}, ledgerId={}, canceledAmount={}, totalBalance={}",
                command.memberId(), command.ledgerId(),
                canceledAmount.getValue(), totalBalance.getValue());

        return CancelEarnPointResult.builder()
                .ledgerId(command.ledgerId())
                .memberId(command.memberId())
                .canceledAmount(canceledAmount.getValue())
                .totalBalance(totalBalance.getValue())
                .build();
    }
}
