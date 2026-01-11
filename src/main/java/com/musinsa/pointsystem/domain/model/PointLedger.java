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
    private final Long earnedAmount;
    private Long availableAmount;
    private Long usedAmount;
    private final EarnType earnType;
    private final UUID sourceTransactionId;
    private final LocalDateTime expiredAt;
    private boolean isCanceled;
    private final LocalDateTime createdAt;

    @Builder
    public PointLedger(UUID id, UUID memberId, Long earnedAmount, Long availableAmount,
                       Long usedAmount, EarnType earnType, UUID sourceTransactionId,
                       LocalDateTime expiredAt, boolean isCanceled, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.earnedAmount = earnedAmount;
        this.availableAmount = availableAmount;
        this.usedAmount = usedAmount;
        this.earnType = earnType;
        this.sourceTransactionId = sourceTransactionId;
        this.expiredAt = expiredAt;
        this.isCanceled = isCanceled;
        this.createdAt = createdAt;
    }

    public static PointLedger create(UUID memberId, Long amount, EarnType earnType, LocalDateTime expiredAt) {
        return PointLedger.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(0L)
                .earnType(earnType)
                .expiredAt(expiredAt)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointLedger createFromCancelUse(UUID memberId, Long amount, EarnType earnType,
                                                   LocalDateTime expiredAt, UUID sourceTransactionId) {
        return PointLedger.builder()
                .id(UuidGenerator.generate())
                .memberId(memberId)
                .earnedAmount(amount)
                .availableAmount(amount)
                .usedAmount(0L)
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
        this.availableAmount = 0L;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isAvailable() {
        return !isCanceled && !isExpired() && availableAmount > 0;
    }

    public Long use(Long amount) {
        Long useAmount = Math.min(amount, availableAmount);
        this.availableAmount -= useAmount;
        this.usedAmount += useAmount;
        return useAmount;
    }

    public void restore(Long amount) {
        if (amount > usedAmount) {
            throw new IllegalStateException("복구할 금액이 사용된 금액보다 클 수 없습니다.");
        }
        this.availableAmount += amount;
        this.usedAmount -= amount;
    }

    public boolean isManual() {
        return earnType == EarnType.MANUAL;
    }
}
