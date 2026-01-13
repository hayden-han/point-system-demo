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
import com.musinsa.pointsystem.domain.service.PointUsageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointUsageManager pointUsageManager;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public UsePointResult execute(UsePointCommand command) {
        log.info("포인트 사용 시작. memberId={}, amount={}, orderId={}",
                command.memberId(), command.amount(), command.orderId());

        // DTO → 도메인 타입 변환 (OrderId VO에서 null/빈값 검증 수행)
        OrderId orderId = OrderId.of(command.orderId());
        PointAmount amount = PointAmount.of(command.amount());

        // 회원 포인트 조회 (사용 가능한 Ledgers만 - DB에서 필터링/정렬 완료)
        MemberPoint memberPoint = memberPointRepository.getOrCreateWithAvailableLedgersForUse(command.memberId());

        // Domain Service를 통한 사용 처리
        MemberPoint.UsageResult usageResult = pointUsageManager.use(memberPoint, amount);

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = usageResult.memberPoint();

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(updatedMemberPoint);

        // 트랜잭션 생성 및 저장
        PointTransaction transaction = pointUsageManager.createUseTransaction(
                command.memberId(),
                amount,
                orderId
        );
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        // 사용 상세 생성 및 저장
        List<PointUsageDetail> usageDetails = pointUsageManager.createUsageDetails(
                savedTransaction.id(),
                usageResult.usageDetails()
        );
        pointUsageDetailRepository.saveAll(usageDetails);

        log.info("포인트 사용 완료. memberId={}, transactionId={}, usedAmount={}, totalBalance={}, usedLedgerCount={}",
                command.memberId(), savedTransaction.id(), amount.getValue(),
                updatedMemberPoint.totalBalance().getValue(), usageDetails.size());

        return UsePointResult.builder()
                .transactionId(savedTransaction.id())
                .memberId(command.memberId())
                .usedAmount(amount.getValue())
                .totalBalance(updatedMemberPoint.totalBalance().getValue())
                .orderId(command.orderId())
                .build();
    }
}
