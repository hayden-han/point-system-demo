package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberPoint {
    private final Long memberId;
    private Long totalBalance;

    @Builder
    public MemberPoint(Long memberId, Long totalBalance) {
        this.memberId = memberId;
        this.totalBalance = totalBalance;
    }

    public static MemberPoint create(Long memberId) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(0L)
                .build();
    }

    public void increaseBalance(Long amount) {
        this.totalBalance += amount;
    }

    public void decreaseBalance(Long amount) {
        if (this.totalBalance < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.totalBalance -= amount;
    }

    public boolean canEarn(Long amount, Long maxBalance) {
        return this.totalBalance + amount <= maxBalance;
    }

    public boolean hasEnoughBalance(Long amount) {
        return this.totalBalance >= amount;
    }
}
