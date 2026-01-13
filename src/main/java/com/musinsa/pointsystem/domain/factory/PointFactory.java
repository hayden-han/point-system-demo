package com.musinsa.pointsystem.domain.factory;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 도메인 객체 생성 팩토리
 * - ID 생성 로직을 캡슐화
 * - 도메인 모델의 순수성 유지
 * - 프레임워크 독립적 (Spring 어노테이션 없음)
 */
public class PointFactory {

    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public PointFactory(IdGenerator idGenerator, TimeProvider timeProvider) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    // === PointLedger 생성 (v2) ===

    /**
     * 포인트 적립건 생성 (v2: EARN Entry 포함)
     */
    public PointLedger createLedger(UUID memberId, PointAmount amount, EarnType earnType, LocalDateTime expiredAt) {
        LocalDateTime now = timeProvider.now();
        return PointLedger.create(
                idGenerator.generate(),
                memberId,
                amount,
                earnType,
                expiredAt,
                null,
                idGenerator,
                now
        );
    }

    /**
     * 사용 취소로 인한 포인트 적립건 생성 (만료된 적립건 복원용, v2)
     */
    public PointLedger createLedgerFromCancelUse(UUID memberId, PointAmount amount, EarnType earnType,
                                                  LocalDateTime expiredAt, UUID sourceLedgerId) {
        LocalDateTime now = timeProvider.now();
        return PointLedger.create(
                idGenerator.generate(),
                memberId,
                amount,
                earnType,
                expiredAt,
                sourceLedgerId,
                idGenerator,
                now
        );
    }

    /**
     * @deprecated v2에서는 createLedger() 사용 권장
     */
    @Deprecated
    public PointLedger createLedgerLegacy(UUID memberId, PointAmount amount, EarnType earnType, LocalDateTime expiredAt) {
        return PointLedger.createLegacy(
                idGenerator.generate(),
                memberId,
                amount,
                earnType,
                expiredAt,
                null,
                timeProvider.now()
        );
    }

    // === PointTransaction 생성 (레거시 - 향후 제거 예정) ===

    /**
     * 적립 트랜잭션 생성
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createEarnTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.EARN,
                amount,
                null,
                null,
                ledgerId,
                timeProvider.now()
        );
    }

    /**
     * 적립 취소 트랜잭션 생성
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createEarnCancelTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.EARN_CANCEL,
                amount,
                null,
                null,
                ledgerId,
                timeProvider.now()
        );
    }

    /**
     * 사용 트랜잭션 생성
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createUseTransaction(UUID memberId, PointAmount amount, OrderId orderId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.USE,
                amount,
                orderId,
                null,
                null,
                timeProvider.now()
        );
    }

    /**
     * 사용 취소 트랜잭션 생성
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointTransaction createUseCancelTransaction(UUID memberId, PointAmount amount,
                                                        OrderId orderId, UUID relatedTransactionId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.USE_CANCEL,
                amount,
                orderId,
                relatedTransactionId,
                null,
                timeProvider.now()
        );
    }

    // === PointUsageDetail 생성 (레거시 - 향후 제거 예정) ===

    /**
     * 포인트 사용 상세 생성
     * @deprecated v2에서는 LedgerEntry로 대체
     */
    @Deprecated
    public PointUsageDetail createUsageDetail(UUID transactionId, UUID ledgerId, PointAmount usedAmount) {
        return new PointUsageDetail(
                idGenerator.generate(),
                transactionId,
                ledgerId,
                usedAmount,
                PointAmount.ZERO
        );
    }

    // === 헬퍼 메서드 ===

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }
}
