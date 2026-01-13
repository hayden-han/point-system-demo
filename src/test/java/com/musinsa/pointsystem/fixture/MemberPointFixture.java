package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;

import java.util.List;
import java.util.UUID;

public class MemberPointFixture {

    public static MemberPoint createDefault() {
        return MemberPoint.create(UuidGenerator.generate());
    }

    public static MemberPoint create(UUID memberId) {
        return MemberPoint.create(memberId);
    }

    public static MemberPoint createWithBalance(UUID memberId, Long balance) {
        // v2: balance는 Ledger들의 합으로 계산되므로 단일 Ledger 생성
        PointLedger ledger = PointLedgerFixture.create(
                UuidGenerator.generate(), memberId, balance,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }

    public static MemberPoint createWithLedgers(UUID memberId, List<PointLedger> ledgers) {
        return MemberPoint.of(memberId, ledgers);
    }

    /**
     * @deprecated v2에서는 balance가 Ledger에서 계산되므로 createWithLedgers 사용
     */
    @Deprecated
    public static MemberPoint createWithLedgers(UUID memberId, Long balance, List<PointLedger> ledgers) {
        return MemberPoint.of(memberId, ledgers);
    }

    public static MemberPoint createNearMaxBalance(UUID memberId, Long maxBalance) {
        PointLedger ledger = PointLedgerFixture.create(
                UuidGenerator.generate(), memberId, maxBalance - 1L,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }

    public static MemberPoint createAtMaxBalance(UUID memberId, Long maxBalance) {
        PointLedger ledger = PointLedgerFixture.create(
                UuidGenerator.generate(), memberId, maxBalance,
                com.musinsa.pointsystem.domain.model.EarnType.SYSTEM);
        return MemberPoint.of(memberId, List.of(ledger));
    }
}
