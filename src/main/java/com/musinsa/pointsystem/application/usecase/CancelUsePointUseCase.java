package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
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

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command) {
        // 원본 트랜잭션 조회
        PointTransaction originalTransaction = pointTransactionRepository.findById(command.getTransactionId())
                .orElseThrow(() -> new PointTransactionNotFoundException(command.getTransactionId()));

        // DTO → 도메인 타입 변환
        PointAmount cancelAmount = PointAmount.of(command.getCancelAmount());

        // 취소 가능한 사용 상세 조회 (만료일 긴 것부터)
        List<PointUsageDetail> usageDetails = pointUsageDetailRepository
                .findCancelableByTransactionId(command.getTransactionId());

        // 정책 조회
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();

        // 사용취소 트랜잭션 생성 및 저장
        PointTransaction cancelTransaction = PointTransaction.createUseCancel(
                command.getMemberId(),
                cancelAmount,
                originalTransaction.getOrderId(),
                command.getTransactionId()
        );
        PointTransaction savedCancelTransaction = pointTransactionRepository.save(cancelTransaction);

        // 회원 포인트 조회 (Ledgers 포함)
        MemberPoint memberPoint = memberPointRepository.findByMemberIdWithLedgers(command.getMemberId())
                .orElseThrow(() -> new MemberPointNotFoundException(command.getMemberId()));

        // Aggregate 메서드 호출 (검증 + 복구 + 잔액 업데이트)
        MemberPoint.RestoreResult restoreResult = memberPoint.cancelUse(
                usageDetails,
                cancelAmount,
                expirationPolicy.getDefaultExpirationDays(),
                savedCancelTransaction.getId()
        );

        // MemberPoint와 Ledgers 함께 저장
        memberPointRepository.save(memberPoint);

        // 사용 상세 저장
        pointUsageDetailRepository.saveAll(restoreResult.updatedUsageDetails());

        return CancelUsePointResult.builder()
                .transactionId(savedCancelTransaction.getId())
                .memberId(command.getMemberId())
                .canceledAmount(cancelAmount.getValue())
                .totalBalance(memberPoint.getTotalBalance().getValue())
                .orderId(originalTransaction.getOrderId().getValue())
                .build();
    }
}
