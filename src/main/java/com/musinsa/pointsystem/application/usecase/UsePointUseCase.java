package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsePointUseCase {

    private final MemberPointService memberPointService;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        // DTO → 도메인 타입 변환 (OrderId VO에서 null/빈값 검증 수행)
        OrderId orderId = OrderId.of(command.getOrderId());
        PointAmount amount = PointAmount.of(command.getAmount());

        // 회원 포인트 조회 (사용 가능한 Ledgers 포함)
        MemberPoint memberPoint = memberPointService.getOrCreateMemberPointWithAvailableLedgers(command.getMemberId());

        // Aggregate 메서드 호출 (잔액 검증 + 적립건 차감 + 잔액 업데이트)
        MemberPoint.UsageResult usageResult = memberPoint.use(amount);

        // MemberPoint와 Ledgers 함께 저장
        memberPointService.saveMemberPoint(memberPoint);

        // 트랜잭션 생성 및 저장
        PointTransaction transaction = PointTransaction.createUse(
                command.getMemberId(),
                amount,
                orderId
        );
        PointTransaction savedTransaction = memberPointService.saveTransaction(transaction);

        // 사용 상세 생성 및 저장
        List<PointUsageDetail> usageDetails = usageResult.usageDetails().stream()
                .map(detail -> PointUsageDetail.create(
                        savedTransaction.getId(),
                        detail.ledgerId(),
                        detail.usedAmount()
                ))
                .toList();
        memberPointService.saveUsageDetails(usageDetails);

        return UsePointResult.builder()
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .usedAmount(amount.getValue())
                .totalBalance(memberPoint.getTotalBalance().getValue())
                .orderId(command.getOrderId())
                .build();
    }
}
