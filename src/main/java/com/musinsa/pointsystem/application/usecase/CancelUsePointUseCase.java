package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.ExpirationPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.*;
import com.musinsa.pointsystem.domain.service.PointRestorePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancelUsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointRestorePolicy pointRestorePolicy;

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

        // 취소 가능 금액 검증
        PointAmount totalCancelable = usageDetails.stream()
                .map(PointUsageDetail::getCancelableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);

        if (cancelAmount.isGreaterThan(totalCancelable)) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), totalCancelable.getValue());
        }

        // 정책 조회 (1회 쿼리)
        ExpirationPolicyConfig expirationPolicy = pointPolicyRepository.getExpirationPolicyConfig();

        // 사용취소 트랜잭션 생성
        PointTransaction cancelTransaction = PointTransaction.createUseCancel(
                command.getMemberId(),
                cancelAmount,
                originalTransaction.getOrderId(),
                command.getTransactionId()
        );
        PointTransaction savedCancelTransaction = pointTransactionRepository.save(cancelTransaction);

        // 관련 적립건 조회
        List<UUID> ledgerIds = usageDetails.stream()
                .map(PointUsageDetail::getLedgerId)
                .distinct()
                .toList();
        Map<UUID, PointLedger> ledgerMap = pointLedgerRepository.findAllById(ledgerIds).stream()
                .collect(Collectors.toMap(PointLedger::getId, ledger -> ledger));

        // 도메인 서비스로 복구 처리
        PointRestorePolicy.RestoreResult restoreResult = pointRestorePolicy.restore(
                usageDetails,
                ledgerMap,
                cancelAmount,
                expirationPolicy.getDefaultExpirationDays(),
                savedCancelTransaction.getId(),
                command.getMemberId()
        );

        // 저장
        pointUsageDetailRepository.saveAll(restoreResult.updatedUsageDetails());
        pointLedgerRepository.saveAll(restoreResult.restoredLedgers());

        // 신규 적립건 일괄 생성 및 저장
        if (!restoreResult.newLedgers().isEmpty()) {
            List<PointLedger> newLedgers = restoreResult.newLedgers().stream()
                    .map(newLedgerInfo -> PointLedger.createFromCancelUse(
                            newLedgerInfo.memberId(),
                            newLedgerInfo.amount(),
                            newLedgerInfo.earnType(),
                            newLedgerInfo.expiredAt(),
                            newLedgerInfo.relatedTransactionId()
                    ))
                    .toList();
            pointLedgerRepository.saveAll(newLedgers);
        }

        // 회원 잔액 업데이트
        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());
        memberPoint.increaseBalance(cancelAmount);
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return CancelUsePointResult.builder()
                .transactionId(savedCancelTransaction.getId())
                .memberId(command.getMemberId())
                .canceledAmount(cancelAmount.getValue())
                .totalBalance(savedMemberPoint.getTotalBalance().getValue())
                .orderId(originalTransaction.getOrderId().getValue())
                .build();
    }
}
