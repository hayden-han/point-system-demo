package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 적립 도메인 매니저
 * - 적립 및 적립취소 관련 도메인 로직 담당
 * - 검증은 Aggregate(MemberPoint)에 위임
 * - 객체 생성은 Factory에 위임
 * - 프레임워크 독립적 (Spring 어노테이션 없음)
 */
public class PointAccrualManager {

    private final PointFactory pointFactory;
    private final TimeProvider timeProvider;

    public PointAccrualManager(PointFactory pointFactory, TimeProvider timeProvider) {
        this.pointFactory = pointFactory;
        this.timeProvider = timeProvider;
    }

    // =====================================================
    // 적립 (Earn) - v2
    // =====================================================

    /**
     * 포인트 적립 (v2: LedgerEntry 자동 생성)
     */
    public MemberPoint.EarnResult earnV2(MemberPoint memberPoint,
                                          PointAmount amount,
                                          EarnType earnType,
                                          LocalDateTime expiredAt,
                                          EarnPolicyConfig policy) {
        LocalDateTime now = timeProvider.now();

        // 검증은 Aggregate에 위임
        memberPoint.validateEarn(amount, policy, now);

        // Ledger 생성 (EARN Entry 자동 포함)
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
     * 만료일 검증 포함 포인트 적립 (v2)
     */
    public MemberPoint.EarnResult earnWithExpirationValidationV2(MemberPoint memberPoint,
                                                                   PointAmount amount,
                                                                   EarnType earnType,
                                                                   Integer expirationDays,
                                                                   EarnPolicyConfig policy) {
        LocalDateTime now = timeProvider.now();

        // 검증은 Aggregate에 위임
        memberPoint.validateEarnWithExpiration(amount, expirationDays, policy, now);

        // 만료일 계산
        LocalDateTime expiredAt = policy.calculateExpirationDate(expirationDays);

        // Ledger 생성 (EARN Entry 자동 포함)
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
    // 적립취소 (Cancel Earn) - v2
    // =====================================================

    /**
     * 적립 취소 (v2: EARN_CANCEL Entry 생성)
     */
    public MemberPoint.CancelEarnResult cancelEarnV2(MemberPoint memberPoint, UUID ledgerId) {
        LocalDateTime now = timeProvider.now();
        IdGenerator idGenerator = pointFactory.getIdGenerator();
        return memberPoint.cancelEarn(ledgerId, idGenerator, now);
    }

}
