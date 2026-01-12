package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 도메인 서비스
 * - ID 생성이 필요한 도메인 로직을 캡슐화
 * - 검증은 Aggregate(MemberPoint)에 위임
 * - 객체 생성은 Factory에 위임
 */
@Service
public class PointDomainService {

    private final PointFactory pointFactory;

    public PointDomainService(PointFactory pointFactory) {
        this.pointFactory = pointFactory;
    }

    // =====================================================
    // 적립 (Earn)
    // =====================================================

    /**
     * 포인트 적립
     * - 검증: MemberPoint.validateEarn()
     * - 생성: PointFactory.createLedger()
     * - 상태 변경: MemberPoint.addLedger()
     */
    public MemberPoint.EarnResult earn(MemberPoint memberPoint,
                                        PointAmount amount,
                                        EarnType earnType,
                                        LocalDateTime expiredAt,
                                        EarnPolicyConfig policy) {
        // 검증은 Aggregate에 위임
        memberPoint.validateEarn(amount, policy);

        // Ledger 생성은 Factory 사용
        PointLedger ledger = pointFactory.createLedger(
                memberPoint.memberId(),
                amount,
                earnType,
                expiredAt
        );

        // Aggregate 상태 변경
        MemberPoint updated = memberPoint.addLedger(ledger);

        return new MemberPoint.EarnResult(updated, ledger);
    }

    /**
     * 만료일 검증 포함 포인트 적립
     */
    public MemberPoint.EarnResult earnWithExpirationValidation(MemberPoint memberPoint,
                                                                PointAmount amount,
                                                                EarnType earnType,
                                                                Integer expirationDays,
                                                                EarnPolicyConfig policy) {
        // 검증은 Aggregate에 위임
        memberPoint.validateEarnWithExpiration(amount, expirationDays, policy);

        // 만료일 계산
        LocalDateTime expiredAt = policy.calculateExpirationDate(expirationDays);

        // Ledger 생성은 Factory 사용
        PointLedger ledger = pointFactory.createLedger(
                memberPoint.memberId(),
                amount,
                earnType,
                expiredAt
        );

        // Aggregate 상태 변경
        MemberPoint updated = memberPoint.addLedger(ledger);

        return new MemberPoint.EarnResult(updated, ledger);
    }

    // =====================================================
    // 사용 (Use)
    // =====================================================

    /**
     * 포인트 사용
     * - MemberPoint.use()는 ID 생성이 불필요하므로 그대로 위임
     */
    public MemberPoint.UsageResult use(MemberPoint memberPoint, PointAmount amount) {
        return memberPoint.use(amount);
    }

    /**
     * 사용 상세 생성
     * - PointTransaction 저장 후 호출
     */
    public List<PointUsageDetail> createUsageDetails(UUID transactionId,
                                                      List<MemberPoint.UsageDetail> usageDetails) {
        return usageDetails.stream()
                .map(detail -> pointFactory.createUsageDetail(
                        transactionId,
                        detail.ledgerId(),
                        detail.usedAmount()
                ))
                .toList();
    }

    // =====================================================
    // 적립취소 (Cancel Earn)
    // =====================================================

    /**
     * 적립 취소
     * - MemberPoint.cancelEarn()은 ID 생성이 불필요하므로 그대로 위임
     */
    public MemberPoint.CancelEarnResult cancelEarn(MemberPoint memberPoint, UUID ledgerId) {
        return memberPoint.cancelEarn(ledgerId);
    }

    // =====================================================
    // 사용취소 (Cancel Use)
    // =====================================================

    /**
     * 사용 취소
     * - 만료된 적립건에 대해 신규 Ledger 생성이 필요
     */
    public MemberPoint.RestoreResult cancelUse(MemberPoint memberPoint,
                                                List<PointUsageDetail> usageDetails,
                                                PointAmount cancelAmount,
                                                int defaultExpirationDays,
                                                UUID cancelTransactionId) {
        return memberPoint.cancelUse(
                usageDetails,
                cancelAmount,
                defaultExpirationDays,
                cancelTransactionId,
                pointFactory
        );
    }

    // =====================================================
    // 트랜잭션 생성
    // =====================================================

    public PointTransaction createEarnTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return pointFactory.createEarnTransaction(memberId, amount, ledgerId);
    }

    public PointTransaction createEarnCancelTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return pointFactory.createEarnCancelTransaction(memberId, amount, ledgerId);
    }

    public PointTransaction createUseTransaction(UUID memberId, PointAmount amount, OrderId orderId) {
        return pointFactory.createUseTransaction(memberId, amount, orderId);
    }

    public PointTransaction createUseCancelTransaction(UUID memberId, PointAmount amount,
                                                        OrderId orderId, UUID relatedTransactionId) {
        return pointFactory.createUseCancelTransaction(memberId, amount, orderId, relatedTransactionId);
    }
}
