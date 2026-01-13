package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
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
    private final TimeProvider timeProvider;

    public PointUsageManager(PointFactory pointFactory, TimeProvider timeProvider) {
        this.pointFactory = pointFactory;
        this.timeProvider = timeProvider;
    }

    // =====================================================
    // 사용 (Use) - v2
    // =====================================================

    /**
     * 포인트 사용 (v2: orderId, LedgerEntry 자동 생성)
     */
    public MemberPoint.UsageResult useV2(MemberPoint memberPoint, PointAmount amount, String orderId) {
        LocalDateTime now = timeProvider.now();
        IdGenerator idGenerator = pointFactory.getIdGenerator();
        return memberPoint.use(amount, orderId, idGenerator, now);
    }

    // =====================================================
    // 사용취소 (Cancel Use) - v2
    // =====================================================

    /**
     * 사용 취소 (v2: LedgerEntry 기반)
     * - PointUsageDetail 불필요
     * - orderId로 해당 주문의 사용 내역 추적
     */
    public MemberPoint.CancelUseResult cancelUseV2(MemberPoint memberPoint,
                                                     String orderId,
                                                     PointAmount cancelAmount,
                                                     int defaultExpirationDays) {
        LocalDateTime now = timeProvider.now();
        IdGenerator idGenerator = pointFactory.getIdGenerator();
        return memberPoint.cancelUse(orderId, cancelAmount, now, idGenerator, defaultExpirationDays);
    }

    // =====================================================
    // 레거시 메서드 (deprecated)
    // =====================================================

    /**
     * @deprecated useV2() 사용 권장
     */
    @Deprecated
    public MemberPoint.UsageResult use(MemberPoint memberPoint, PointAmount amount) {
        return memberPoint.use(amount);
    }

    /**
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
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

    /**
     * @deprecated cancelUseV2() 사용 권장
     */
    @Deprecated
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
                pointFactory,
                timeProvider.now()
        );
    }

    // =====================================================
    // 트랜잭션 생성 (레거시 - 향후 제거 예정)
    // =====================================================

    /**
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createUseTransaction(UUID memberId, PointAmount amount, OrderId orderId) {
        return pointFactory.createUseTransaction(memberId, amount, orderId);
    }

    /**
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createUseCancelTransaction(UUID memberId, PointAmount amount,
                                                        OrderId orderId, UUID relatedTransactionId) {
        return pointFactory.createUseCancelTransaction(memberId, amount, orderId, relatedTransactionId);
    }
}
