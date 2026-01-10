package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.*;
import com.musinsa.pointsystem.application.port.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelUsePointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public CancelUsePointResult execute(CancelUsePointCommand command) {
        PointTransaction originalTransaction = pointTransactionRepository.findById(command.getTransactionId())
                .orElseThrow(() -> new PointTransactionNotFoundException(command.getTransactionId()));

        // 취소 가능한 사용 상세 조회 (만료일 긴 것부터)
        List<PointUsageDetail> usageDetails = pointUsageDetailRepository
                .findCancelableByTransactionId(command.getTransactionId());

        Long totalCancelable = usageDetails.stream()
                .mapToLong(PointUsageDetail::getCancelableAmount)
                .sum();

        if (command.getCancelAmount() > totalCancelable) {
            throw new InvalidCancelAmountException(command.getCancelAmount(), totalCancelable);
        }

        Long defaultDays = pointPolicyRepository.getValueByKey(PointPolicy.EXPIRATION_DEFAULT_DAYS);

        // 사용취소 트랜잭션 생성
        PointTransaction cancelTransaction = PointTransaction.createUseCancel(
                command.getMemberId(),
                command.getCancelAmount(),
                originalTransaction.getOrderId(),
                command.getTransactionId()
        );
        PointTransaction savedCancelTransaction = pointTransactionRepository.save(cancelTransaction);

        Long remainingCancelAmount = command.getCancelAmount();
        List<PointUsageDetail> updatedUsageDetails = new ArrayList<>();
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<PointLedger> newLedgers = new ArrayList<>();

        for (PointUsageDetail usageDetail : usageDetails) {
            if (remainingCancelAmount <= 0) {
                break;
            }

            Long cancelFromDetail = usageDetail.cancel(remainingCancelAmount);
            remainingCancelAmount -= cancelFromDetail;
            updatedUsageDetails.add(usageDetail);

            // 원본 적립건 조회
            PointLedger originalLedger = pointLedgerRepository.findById(usageDetail.getLedgerId())
                    .orElseThrow(() -> new IllegalStateException("적립건을 찾을 수 없습니다: " + usageDetail.getLedgerId()));

            if (originalLedger.isExpired()) {
                // 만료된 적립건은 신규 적립으로 처리
                PointLedger newLedger = PointLedger.createFromCancelUse(
                        command.getMemberId(),
                        cancelFromDetail,
                        originalLedger.getEarnType(),
                        LocalDateTime.now().plusDays(defaultDays),
                        savedCancelTransaction.getId()
                );
                newLedgers.add(newLedger);
            } else {
                // 만료되지 않은 적립건은 복구
                originalLedger.restore(cancelFromDetail);
                updatedLedgers.add(originalLedger);
            }
        }

        // 저장
        pointUsageDetailRepository.saveAll(updatedUsageDetails);
        pointLedgerRepository.saveAll(updatedLedgers);
        for (PointLedger newLedger : newLedgers) {
            pointLedgerRepository.save(newLedger);
        }

        // 회원 잔액 업데이트
        MemberPoint memberPoint = memberPointRepository.getOrCreate(command.getMemberId());
        memberPoint.increaseBalance(command.getCancelAmount());
        MemberPoint savedMemberPoint = memberPointRepository.save(memberPoint);

        return CancelUsePointResult.builder()
                .transactionId(savedCancelTransaction.getId())
                .memberId(command.getMemberId())
                .canceledAmount(command.getCancelAmount())
                .totalBalance(savedMemberPoint.getTotalBalance())
                .orderId(originalTransaction.getOrderId())
                .build();
    }
}
