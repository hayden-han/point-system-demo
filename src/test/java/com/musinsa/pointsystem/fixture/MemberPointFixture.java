package com.musinsa.pointsystem.fixture;

import com.musinsa.pointsystem.domain.model.MemberPoint;

public class MemberPointFixture {

    public static MemberPoint createDefault() {
        return MemberPoint.builder()
                .memberId(1L)
                .totalBalance(0L)
                .build();
    }

    public static MemberPoint create(Long memberId) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(0L)
                .build();
    }

    public static MemberPoint createWithBalance(Long memberId, Long balance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(balance)
                .build();
    }

    public static MemberPoint createNearMaxBalance(Long memberId, Long maxBalance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(maxBalance - 1L)
                .build();
    }

    public static MemberPoint createAtMaxBalance(Long memberId, Long maxBalance) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(maxBalance)
                .build();
    }
}
