package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
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
        if (command.getOrderId() == null || command.getOrderId().isBlank()) {
            throw new InvalidOrderIdException();
        }

        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());

        if (!memberPoint.hasEnoughBalance(command.getAmount())) {
            throw new InsufficientPointException(command.getAmount(), memberPoint.getTotalBalance());
        }

        // 사용 가능한 적립건 조회 (우선순위: 수기지급 > 만료일 짧은 순)
        List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(command.getMemberId());

        // 트랜잭션 생성
        PointTransaction transaction = PointTransaction.createUse(
                command.getMemberId(),
                command.getAmount(),
                command.getOrderId()
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        // 도메인 서비스로 적립건에서 차감
        PointUsagePolicy.UsageResult usageResult = pointUsagePolicy.use(availableLedgers, command.getAmount());

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
        memberPoint.decreaseBalance(command.getAmount());
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return UsePointResult.builder()
                .transactionId(savedTransaction.getId())
                .memberId(command.getMemberId())
                .usedAmount(command.getAmount())
                .totalBalance(savedMemberPoint.getTotalBalance())
                .orderId(command.getOrderId())
                .build();
    }
}
