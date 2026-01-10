package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.time.LocalDateTime;

public class PointLedgerFixture {

    public static PointLedger createDefault() {
        return PointLedger.builder()
                .id(1L)
                .memberId(1L)
                .earnedAmount(1000L)
                .availableAmount(1000L)
                .usedAmount(0L)
                .earnType(EarnType.SYSTEM)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger create(Long id, Long memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(0L)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createWithExpiration(Long id, Long memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(0L)
                .earnType(earnType)
                .expiredAt(expiredAt)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createPartiallyUsed(Long id, Long memberId, Long earnedAmount,
                                                   Long usedAmount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(earnedAmount)
                .availableAmount(earnedAmount - usedAmount)
                .usedAmount(usedAmount)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createFullyUsed(Long id, Long memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(0L)
                .usedAmount(amount)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createCanceled(Long id, Long memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(0L)
                .usedAmount(0L)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .isCanceled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createExpired(Long id, Long memberId, Long amount, EarnType earnType) {
        return PointLedger.builder()
                .id(id)
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(0L)
                .earnType(earnType)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .isCanceled(false)
                .createdAt(LocalDateTime.now().minusDays(366))
                .build();
    }

    public static PointLedger createManual(Long id, Long memberId, Long amount) {
        return create(id, memberId, amount, EarnType.MANUAL);
    }

    public static PointLedger createSystem(Long id, Long memberId, Long amount) {
        return create(id, memberId, amount, EarnType.SYSTEM);
    }
}
