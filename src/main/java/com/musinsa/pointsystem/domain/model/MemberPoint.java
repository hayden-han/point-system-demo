package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MemberPoint {
    private final UUID memberId;
    private PointAmount totalBalance;

    @Builder
    public MemberPoint(UUID memberId, PointAmount totalBalance) {
        this.memberId = memberId;
        this.totalBalance = totalBalance != null ? totalBalance : PointAmount.ZERO;
    }

    public static MemberPoint create(UUID memberId) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(PointAmount.ZERO)
                .build();
    }

    public void increaseBalance(PointAmount amount) {
        this.totalBalance = this.totalBalance.add(amount);
    }

    /**
     * 잔액 차감
     * - 잔액 부족 시: InsufficientPointException
     */
    public void decreaseBalance(PointAmount amount) {
        if (this.totalBalance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), this.totalBalance.getValue());
        }
        this.totalBalance = this.totalBalance.subtract(amount);
    }

    public boolean canEarn(PointAmount amount, PointAmount maxBalance) {
        return this.totalBalance.add(amount).isLessThanOrEqual(maxBalance);
    }

    public boolean hasEnoughBalance(PointAmount amount) {
        return this.totalBalance.isGreaterThanOrEqual(amount);
    }
}
