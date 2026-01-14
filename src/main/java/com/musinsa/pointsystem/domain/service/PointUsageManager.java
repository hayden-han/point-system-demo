package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;

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
    // 사용 (Use)
    // =====================================================

    /**
     * 포인트 사용 (LedgerEntry 자동 생성)
     */
    public MemberPoint.UsageResult useV2(MemberPoint memberPoint, PointAmount amount, String orderId) {
        LocalDateTime now = timeProvider.now();
        IdGenerator idGenerator = pointFactory.getIdGenerator();
        return memberPoint.use(amount, orderId, idGenerator, now);
    }

    // =====================================================
    // 사용취소 (Cancel Use)
    // =====================================================

    /**
     * 사용 취소 (LedgerEntry 기반)
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
}
