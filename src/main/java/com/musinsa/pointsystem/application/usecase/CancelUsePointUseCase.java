package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.domain.service.PointUsageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelUsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointUsageManager pointUsageManager;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command) {
        // 원본 트랜잭션 조회 (없으면 PointTransactionNotFoundException 발생)
        PointTransaction originalTransaction = pointTransactionRepository.getById(command.transactionId());

        // DTO → 도메인 타입 변환
        PointAmount cancelAmount = PointAmount.of(command.cancelAmount());

        // 취소 가능한 사용 상세 조회 (만료일 긴 것부터)
        List<PointUsageDetail> usageDetails = pointUsageDetailRepository
                .findCancelableByTransactionId(command.transactionId());

        // 정책 조회
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();

        // 사용취소 트랜잭션 생성 및 저장
        PointTransaction cancelTransaction = pointUsageManager.createUseCancelTransaction(
                command.memberId(),
                cancelAmount,
                originalTransaction.orderId(),
                command.transactionId()
        );
        PointTransaction savedCancelTransaction = pointTransactionRepository.save(cancelTransaction);

        // 회원 포인트 조회 (모든 Ledgers 포함 - 복원 대상 Ledger 필요)
        MemberPoint memberPoint = memberPointRepository.getByMemberIdWithAllLedgers(command.memberId());

        // Domain Service를 통한 사용 취소 처리
        MemberPoint.RestoreResult restoreResult = pointUsageManager.cancelUse(
                memberPoint,
                usageDetails,
                cancelAmount,
                expirationPolicy.defaultExpirationDays(),
                savedCancelTransaction.id()
        );

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = restoreResult.memberPoint();

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(updatedMemberPoint);

        // 사용 상세 저장
        pointUsageDetailRepository.saveAll(restoreResult.updatedUsageDetails());

        return CancelUsePointResult.builder()
                .transactionId(savedCancelTransaction.id())
                .memberId(command.memberId())
                .canceledAmount(cancelAmount.getValue())
                .totalBalance(updatedMemberPoint.totalBalance().getValue())
                .orderId(originalTransaction.orderId().getValue())
                .build();
    }
}
