package com.musinsa.pointsystem.domain.factory;

import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 도메인 객체 생성 팩토리
 * - ID 생성 로직을 캡슐화
 * - 도메인 모델의 순수성 유지
 */
@Component
public class PointFactory {

    private final IdGenerator idGenerator;

    public PointFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    // === PointLedger 생성 ===

    /**
     * 포인트 적립건 생성
     */
    public PointLedger createLedger(UUID memberId, PointAmount amount, EarnType earnType, LocalDateTime expiredAt) {
        return new PointLedger(
                idGenerator.generate(),
                memberId,
                amount,
                amount,
                PointAmount.ZERO,
                earnType,
                null,
                expiredAt,
                false,
                LocalDateTime.now()
        );
    }

    /**
     * 사용 취소로 인한 포인트 적립건 생성 (만료된 적립건 복원용)
     */
    public PointLedger createLedgerFromCancelUse(UUID memberId, PointAmount amount, EarnType earnType,
                                                  LocalDateTime expiredAt, UUID sourceTransactionId) {
        return new PointLedger(
                idGenerator.generate(),
                memberId,
                amount,
                amount,
                PointAmount.ZERO,
                earnType,
                sourceTransactionId,
                expiredAt,
                false,
                LocalDateTime.now()
        );
    }

    // === PointTransaction 생성 ===

    /**
     * 적립 트랜잭션 생성
     */
    public PointTransaction createEarnTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.EARN,
                amount,
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    /**
     * 적립 취소 트랜잭션 생성
     */
    public PointTransaction createEarnCancelTransaction(UUID memberId, PointAmount amount, UUID ledgerId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.EARN_CANCEL,
                amount,
                null,
                null,
                ledgerId,
                LocalDateTime.now()
        );
    }

    /**
     * 사용 트랜잭션 생성
     */
    public PointTransaction createUseTransaction(UUID memberId, PointAmount amount, OrderId orderId) {
        return new PointTransaction(
                idGenerator.generate(),
                memberId,
                TransactionType.USE,
                amount,
                orderId,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 사용 취소 트랜잭션 생성
     */
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
                LocalDateTime.now()
        );
    }

    // === PointUsageDetail 생성 ===

    /**
     * 포인트 사용 상세 생성
     */
    public PointUsageDetail createUsageDetail(UUID transactionId, UUID ledgerId, PointAmount usedAmount) {
        return new PointUsageDetail(
                idGenerator.generate(),
                transactionId,
                ledgerId,
                usedAmount,
                PointAmount.ZERO
        );
    }
}
