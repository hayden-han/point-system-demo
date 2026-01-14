package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.port.TimeProvider;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.service.PointUsageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointUsageManager pointUsageManager;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        log.info("포인트 사용 시작. memberId={}, amount={}, orderId={}",
                command.memberId(), command.amount(), command.orderId());

        LocalDateTime now = timeProvider.now();

        // DTO → 도메인 타입 변환 (OrderId VO에서 null/빈값 검증 수행)
        OrderId.of(command.orderId()); // 검증용
        PointAmount amount = PointAmount.of(command.amount());

        // 회원 포인트 조회 (Entry 포함, 사용 가능한 Ledger만)
        MemberPoint memberPoint = memberPointRepository
                .findByMemberIdWithAvailableLedgersAndEntries(command.memberId(), now)
                .orElseGet(() -> MemberPoint.create(command.memberId()));

        // Domain Service를 통한 사용 처리 (USE Entry 자동 생성)
        MemberPoint.UsageResult usageResult = pointUsageManager.use(memberPoint, amount, command.orderId());

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = usageResult.memberPoint();

        // MemberPoint + Ledgers + Entries 저장
        memberPointRepository.saveWithEntries(updatedMemberPoint);

        log.info("포인트 사용 완료. memberId={}, usedAmount={}, totalBalance={}, usedLedgerCount={}",
                command.memberId(), amount.getValue(),
                updatedMemberPoint.getTotalBalance(now).getValue(), usageResult.usageDetails().size());

        return UsePointResult.builder()
                .memberId(command.memberId())
                .usedAmount(amount.getValue())
                .totalBalance(updatedMemberPoint.getTotalBalance(now).getValue())
                .orderId(command.orderId())
                .build();
    }
}
