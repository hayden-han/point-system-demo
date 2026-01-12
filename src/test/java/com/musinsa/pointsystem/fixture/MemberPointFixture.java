package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.util.List;
import java.util.UUID;

public class MemberPointFixture {

    public static MemberPoint createDefault() {
        return new MemberPoint(
                UuidGenerator.generate(),
                PointAmount.ZERO,
                List.of()
        );
    }

    public static MemberPoint create(UUID memberId) {
        return new MemberPoint(
                memberId,
                PointAmount.ZERO,
                List.of()
        );
    }

    public static MemberPoint createWithBalance(UUID memberId, Long balance) {
        return new MemberPoint(
                memberId,
                PointAmount.of(balance),
                List.of()
        );
    }

    public static MemberPoint createWithLedgers(UUID memberId, Long balance, List<PointLedger> ledgers) {
        return new MemberPoint(
                memberId,
                PointAmount.of(balance),
                ledgers
        );
    }

    public static MemberPoint createNearMaxBalance(UUID memberId, Long maxBalance) {
        return new MemberPoint(
                memberId,
                PointAmount.of(maxBalance - 1L),
                List.of()
        );
    }

    public static MemberPoint createAtMaxBalance(UUID memberId, Long maxBalance) {
        return new MemberPoint(
                memberId,
                PointAmount.of(maxBalance),
                List.of()
        );
    }
}
