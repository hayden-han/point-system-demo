package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.time.LocalDateTime;
import java.util.UUID;

public class PointLedgerFixture {

    public static PointLedger createDefault() {
        return new PointLedger(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1000L,
                1000L,
                EarnType.SYSTEM,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now()
        );
    }

    public static PointLedger create(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                amount,
                amount,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now()
        );
    }

    public static PointLedger createWithExpiration(UUID id, UUID memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt) {
        return new PointLedger(
                id,
                memberId,
                amount,
                amount,
                earnType,
                null,
                expiredAt,
                false,
                LocalDateTime.now()
        );
    }

    public static PointLedger createWithExpiration(UUID id, UUID memberId, Long amount,
                                                    EarnType earnType, LocalDateTime expiredAt,
                                                    LocalDateTime earnedAt) {
        return new PointLedger(
                id,
                memberId,
                amount,
                amount,
                earnType,
                null,
                expiredAt,
                false,
                earnedAt
        );
    }

    public static PointLedger createPartiallyUsed(UUID id, UUID memberId, Long earnedAmount,
                                                   Long usedAmount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                earnedAmount,
                earnedAmount - usedAmount,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now()
        );
    }

    public static PointLedger createFullyUsed(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                amount,
                0L,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                false,
                LocalDateTime.now()
        );
    }

    public static PointLedger createCanceled(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                amount,
                0L,
                earnType,
                null,
                LocalDateTime.now().plusDays(365),
                true,
                LocalDateTime.now()
        );
    }

    public static PointLedger createExpired(UUID id, UUID memberId, Long amount, EarnType earnType) {
        return new PointLedger(
                id,
                memberId,
                amount,
                amount,
                earnType,
                null,
                LocalDateTime.now().minusDays(1),
                false,
                LocalDateTime.now().minusDays(366)
        );
    }

    public static PointLedger createManual(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.MANUAL);
    }

    public static PointLedger createSystem(UUID id, UUID memberId, Long amount) {
        return create(id, memberId, amount, EarnType.SYSTEM);
    }
}
