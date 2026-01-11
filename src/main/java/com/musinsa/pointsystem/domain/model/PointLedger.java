package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PointLedger {
    private final UUID id;
    private final UUID memberId;
    private final PointAmount earnedAmount;
    private PointAmount availableAmount;
    private PointAmount usedAmount;
    private final EarnType earnType;
    private final UUID sourceTransactionId;
    private final LocalDateTime expiredAt;
    private boolean isCanceled;
    private final LocalDateTime createdAt;

    @Builder
    public PointLedger(UUID id, UUID memberId, PointAmount earnedAmount, PointAmount availableAmount,
                       PointAmount usedAmount, EarnType earnType, UUID sourceTransactionId,
                       LocalDateTime expiredAt, boolean isCanceled, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.earnedAmount = earnedAmount;
        this.availableAmount = availableAmount != null ? availableAmount : PointAmount.ZERO;
        this.usedAmount = usedAmount != null ? usedAmount : PointAmount.ZERO;
        this.earnType = earnType;
        this.sourceTransactionId = sourceTransactionId;
        this.expiredAt = expiredAt;
        this.isCanceled = isCanceled;
        this.createdAt = createdAt;
    }

    public static PointLedger create(UUID memberId, PointAmount amount, EarnType earnType, LocalDateTime expiredAt) {
        return PointLedger.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .expiredAt(expiredAt)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createFromCancelUse(UUID memberId, PointAmount amount, EarnType earnType,
                                                   LocalDateTime expiredAt, UUID sourceTransactionId) {
        return PointLedger.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(PointAmount.ZERO)
                .earnType(earnType)
                .sourceTransactionId(sourceTransactionId)
                .expiredAt(expiredAt)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean canCancel() {
        return !isCanceled && earnedAmount.equals(availableAmount);
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("이미 사용된 적립건은 취소할 수 없습니다.");
        }
        this.isCanceled = true;
        this.availableAmount = PointAmount.ZERO;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isAvailable() {
        return !isCanceled && !isExpired() && availableAmount.isPositive();
    }

    public PointAmount use(PointAmount amount) {
        PointAmount useAmount = amount.min(availableAmount);
        this.availableAmount = this.availableAmount.subtract(useAmount);
        this.usedAmount = this.usedAmount.add(useAmount);
        return useAmount;
    }

    public void restore(PointAmount amount) {
        if (amount.isGreaterThan(usedAmount)) {
            throw new IllegalStateException("복구할 금액이 사용된 금액보다 클 수 없습니다.");
        }
        this.availableAmount = this.availableAmount.add(amount);
        this.usedAmount = this.usedAmount.subtract(amount);
    }

    public boolean isManual() {
        return earnType == EarnType.MANUAL;
    }
}
