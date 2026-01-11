package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PointUsageDetail {
    private final UUID id;
    private final UUID transactionId;
    private final UUID ledgerId;
    private final PointAmount usedAmount;
    private PointAmount canceledAmount;
    private final LocalDateTime createdAt;

    @Builder
    public PointUsageDetail(UUID id, UUID transactionId, UUID ledgerId,
                            PointAmount usedAmount, PointAmount canceledAmount, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
        this.usedAmount = usedAmount;
        this.canceledAmount = canceledAmount != null ? canceledAmount : PointAmount.ZERO;
        this.createdAt = createdAt;
    }

    public static PointUsageDetail create(UUID transactionId, UUID ledgerId, PointAmount usedAmount) {
        return PointUsageDetail.builder()
                .id(UuidGenerator.generate())
                .transactionId(transactionId)
                .ledgerId(ledgerId)
                .usedAmount(usedAmount)
                .canceledAmount(PointAmount.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public PointAmount getCancelableAmount() {
        return usedAmount.subtract(canceledAmount);
    }

    public PointAmount cancel(PointAmount amount) {
        PointAmount cancelAmount = amount.min(getCancelableAmount());
        this.canceledAmount = this.canceledAmount.add(cancelAmount);
        return cancelAmount;
    }

    public boolean isCancelable() {
        return getCancelableAmount().isPositive();
    }
}
