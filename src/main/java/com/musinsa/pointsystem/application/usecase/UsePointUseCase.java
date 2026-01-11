package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.domain.service.PointUsagePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointUsagePolicy pointUsagePolicy;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        // DTO → 도메인 타입 변환 (OrderId VO에서 null/빈값 검증 수행)
        OrderId orderId = OrderId.of(command.getOrderId());
        PointAmount amount = PointAmount.of(command.getAmount());

        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());

        if (!memberPoint.hasEnoughBalance(amount)) {
            throw new InsufficientPointException(amount.getValue(), memberPoint.getTotalBalance().getValue());
        }

        // 사용 가능한 적립건 조회 (우선순위: 수기지급 > 만료일 짧은 순)
        List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(command.getMemberId());

        // 트랜잭션 생성
        PointTransaction transaction = PointTransaction.createUse(
                command.getMemberId(),
                amount,
                orderId
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        // 도메인 서비스로 적립건에서 차감
        PointUsagePolicy.UsageResult usageResult = pointUsagePolicy.use(availableLedgers, amount);

        // 사용 상세 생성
        List<PointUsageDetail> usageDetails = usageResult.usageDetails().stream()
                .map(detail -> PointUsageDetail.create(
                        savedTransaction.getId(),
                        detail.ledgerId(),
                        detail.usedAmount()
                ))
                .toList();

        // 저장
        pointLedgerRepository.saveAll(usageResult.updatedLedgers());
        pointUsageDetailRepository.saveAll(usageDetails);

        // 회원 잔액 업데이트
        memberPoint.decreaseBalance(amount);
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return UsePointResult.builder()
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .usedAmount(amount.getValue())
                .totalBalance(savedMemberPoint.getTotalBalance().getValue())
                .orderId(command.getOrderId())
                .build();
    }
}
