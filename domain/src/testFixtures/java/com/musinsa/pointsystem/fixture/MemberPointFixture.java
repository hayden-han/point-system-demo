package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.util.List;
import java.util.UUID;

public class MemberPointFixture {

    public static MemberPoint createDefault() {
        return MemberPoint.create(UUID.randomUUID());
    }

    public static MemberPoint create(UUID memberId) {
        return MemberPoint.create(memberId);
    }

    public static MemberPoint createWithBalance(UUID memberId, Long balance) {
        PointLedger ledger = PointLedgerFixture.create(
                UUID.randomUUID(), memberId, balance,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }

    public static MemberPoint createWithLedgers(UUID memberId, List<PointLedger> ledgers) {
        return MemberPoint.of(memberId, ledgers);
    }

    public static MemberPoint createNearMaxBalance(UUID memberId, Long maxBalance) {
        PointLedger ledger = PointLedgerFixture.create(
                UUID.randomUUID(), memberId, maxBalance - 1L,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }

    public static MemberPoint createAtMaxBalance(UUID memberId, Long maxBalance) {
        PointLedger ledger = PointLedgerFixture.create(
                UUID.randomUUID(), memberId, maxBalance,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }
}
