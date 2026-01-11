package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointLedgerFixture {

    public static PointLedger createDefault() {
        return PointLedger.builder()
                .id(UuidGenerator.generate())
                .memberId(UuidGenerator.generate())
                .earnedAmount(PointAmount.of(1000L))
                .availableAmount(PointAmount.of(1000L))
                .usedAmount(PointAmount.ZERO)
                .earnType(EarnType.SYSTEM)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger create(UUID id, UUID memberId, Long amount, EarnType earnType) {
        PointAmount pointAmount = PointAmount.of(amount);
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(pointAmount)
                .availableAmount(pointAmount)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createWithExpiration(UUID id, UUID memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt) {
        PointAmount pointAmount = PointAmount.of(amount);
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(pointAmount)
                .availableAmount(pointAmount)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .expiredAt(expiredAt)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createPartiallyUsed(UUID id, UUID memberId, Long earnedAmount,
                                                   Long usedAmount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(PointAmount.of(earnedAmount))
                .availableAmount(PointAmount.of(earnedAmount - usedAmount))
                .usedAmount(PointAmount.of(usedAmount))
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createFullyUsed(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(PointAmount.of(amount))
                .availableAmount(PointAmount.ZERO)
                .usedAmount(PointAmount.of(amount))
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createCanceled(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(PointAmount.of(amount))
                .availableAmount(PointAmount.ZERO)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createExpired(UUID id, UUID memberId, Long amount, EarnType earnType) {
        PointAmount pointAmount = PointAmount.of(amount);
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(pointAmount)
                .availableAmount(pointAmount)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .isCanceled(false)
                .createdAt(LocalDateTime.now().minusDays(366))
                .build();
    }

    public static PointLedger createManual(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.MANUAL);
    }

    public static PointLedger createSystem(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.SYSTEM);
    }
}
