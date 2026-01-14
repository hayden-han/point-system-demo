package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.port.TimeProvider;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.service.PointUsageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelUsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointUsageManager pointUsageManager;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command) {
        log.info("포인트 사용취소 시작. memberId={}, orderId={}, cancelAmount={}",
                command.memberId(), command.orderId(), command.cancelAmount());

        LocalDateTime now = timeProvider.now();

        // 1. DTO → 도메인 타입 변환
        PointAmount cancelAmount = PointAmount.of(command.cancelAmount());

        // 2. 정책 조회
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();

        // 3. 회원 포인트 조회 (해당 주문과 관련된 Ledger + Entry)
        MemberPoint memberPoint = memberPointRepository
                .findByMemberIdWithLedgersForOrder(command.memberId(), command.orderId())
                .orElseThrow(() -> new InvalidCancelAmountException(
                        cancelAmount.getValue(), 0L));

        // 4. Domain Service를 통한 사용 취소 처리
        MemberPoint.CancelUseResult cancelResult = pointUsageManager.cancelUse(
                memberPoint,
                command.orderId(),
                cancelAmount,
                expirationPolicy.defaultExpirationDays()
        );

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = cancelResult.memberPoint();

        // 5. MemberPoint + Ledgers + Entries 저장
        memberPointRepository.saveWithEntries(updatedMemberPoint);

        log.info("포인트 사용취소 완료. memberId={}, orderId={}, canceledAmount={}, totalBalance={}, newLedgerCount={}",
                command.memberId(), command.orderId(), cancelAmount.getValue(),
                updatedMemberPoint.getTotalBalance(now).getValue(), cancelResult.newLedgers().size());

        return CancelUsePointResult.builder()
                .memberId(command.memberId())
                .canceledAmount(cancelAmount.getValue())
                .totalBalance(updatedMemberPoint.getTotalBalance(now).getValue())
                .orderId(command.orderId())
                .build();
    }
}
