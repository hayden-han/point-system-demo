package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PointLedgerFixture {

    public static PointLedger createDefault() {
        return new PointLedger(
                UuidGenerator.generate(),
                UuidGenerator.generate(),
                PointAmount.of(1000L),
                PointAmount.of(1000L),
                EarnType.SYSTEM,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger create(UUID id, UUID memberId, Long amount, EarnType earnType) {
        PointAmount pointAmount = PointAmount.of(amount);
        return new PointLedger(
                id,
                memberId,
                pointAmount,
                pointAmount,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger createWithExpiration(UUID id, UUID memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt) {
        PointAmount pointAmount = PointAmount.of(amount);
        return new PointLedger(
                id,
                memberId,
                pointAmount,
                pointAmount,
                earnType,
                null,
                expiredAt,
                false,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger createWithExpiration(UUID id, UUID memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt,
                                                    LocalDateTime earnedAt) {
        PointAmount pointAmount = PointAmount.of(amount);
        return new PointLedger(
                id,
                memberId,
                pointAmount,
                pointAmount,
                earnType,
                null,
                expiredAt,
                false,
                earnedAt,
                List.of()
        );
    }

    public static PointLedger createPartiallyUsed(UUID id, UUID memberId, Long earnedAmount,
                                                   Long usedAmount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                PointAmount.of(earnedAmount),
                PointAmount.of(earnedAmount - usedAmount),
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger createFullyUsed(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                PointAmount.of(amount),
                PointAmount.ZERO,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger createCanceled(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                PointAmount.of(amount),
                PointAmount.ZERO,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                true,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static PointLedger createExpired(UUID id, UUID memberId, Long amount, EarnType earnType) {
        PointAmount pointAmount = PointAmount.of(amount);
        return new PointLedger(
                id,
                memberId,
                pointAmount,
                pointAmount,
                earnType,
                null,
                LocalDateTime.now().minusDays(1),
                false,
                LocalDateTime.now().minusDays(366),
                List.of()
        );
    }

    public static PointLedger createManual(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.MANUAL);
    }

    public static PointLedger createSystem(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.SYSTEM);
    }
}
