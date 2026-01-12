package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.model.*;

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

    public PointAccrualManager(PointFactory pointFactory) {
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
    // 트랜잭션 생성
    // =====================================================

    public PointTransaction createEarnTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return pointFactory.createEarnTransaction(memberId, amount, ledgerId);
    }

    public PointTransaction createEarnCancelTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return pointFactory.createEarnCancelTransaction(memberId, amount, ledgerId);
    }
}
