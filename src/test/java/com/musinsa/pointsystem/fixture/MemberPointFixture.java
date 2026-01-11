package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.util.UUID;

public class MemberPointFixture {

    public static MemberPoint createDefault() {
        return MemberPoint.builder()
                .memberId(UuidGenerator.generate())
                .totalBalance(0L)
                .build();
    }

    public static MemberPoint create(UUID memberId) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(0L)
                .build();
    }

    public static MemberPoint createWithBalance(UUID memberId, Long balance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(balance)
                .build();
    }

    public static MemberPoint createNearMaxBalance(UUID memberId, Long maxBalance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(maxBalance - 1L)
                .build();
    }

    public static MemberPoint createAtMaxBalance(UUID memberId, Long maxBalance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(maxBalance)
                .build();
    }
}
