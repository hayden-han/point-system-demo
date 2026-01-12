package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.*;

import java.util.List;
import java.util.UUID;

/**
 * 포인트 사용 도메인 매니저
 * - 사용 및 사용취소 관련 도메인 로직 담당
 * - 검증은 Aggregate(MemberPoint)에 위임
 * - 객체 생성은 Factory에 위임
 * - 프레임워크 독립적 (Spring 어노테이션 없음)
 */
public class PointUsageManager {

    private final PointFactory pointFactory;

    public PointUsageManager(PointFactory pointFactory) {
        this.pointFactory = pointFactory;
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

    public PointTransaction createUseTransaction(UUID memberId, PointAmount amount, OrderId orderId) {
        return pointFactory.createUseTransaction(memberId, amount, orderId);
    }

    public PointTransaction createUseCancelTransaction(UUID memberId, PointAmount amount,
                                                        OrderId orderId, UUID relatedTransactionId) {
        return pointFactory.createUseCancelTransaction(memberId, amount, orderId, relatedTransactionId);
    }
}
