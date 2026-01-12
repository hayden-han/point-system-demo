package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        // DTO → 도메인 타입 변환 (OrderId VO에서 null/빈값 검증 수행)
        OrderId orderId = OrderId.of(command.orderId());
        PointAmount amount = PointAmount.of(command.amount());

        // 회원 포인트 조회 (사용 가능한 Ledgers 포함)
        MemberPoint memberPoint = memberPointRepository.getOrCreateWithAvailableLedgers(command.memberId());

        // Aggregate 메서드 호출 (불변 - 새 MemberPoint 반환)
        MemberPoint.UsageResult usageResult = memberPoint.use(amount);

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = usageResult.memberPoint();

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(updatedMemberPoint);

        // 트랜잭션 생성 및 저장
        PointTransaction transaction = PointTransaction.createUse(
                command.memberId(),
                amount,
                orderId
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        // 사용 상세 생성 및 저장
        List<PointUsageDetail> usageDetails = usageResult.usageDetails().stream()
                .map(detail -> PointUsageDetail.create(
                        savedTransaction.id(),
                        detail.ledgerId(),
                        detail.usedAmount()
                ))
                .toList();
        pointUsageDetailRepository.saveAll(usageDetails);

        return UsePointResult.builder()
                .transactionId(savedTransaction.id())
                .memberId(command.memberId())
                .usedAmount(amount.getValue())
                .totalBalance(updatedMemberPoint.totalBalance().getValue())
                .orderId(command.orderId())
                .build();
    }
}
