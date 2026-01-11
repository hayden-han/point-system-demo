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
    private final Long usedAmount;
    private Long canceledAmount;
    private final LocalDateTime createdAt;

    @Builder
    public PointUsageDetail(UUID id, UUID transactionId, UUID ledgerId,
                            Long usedAmount, Long canceledAmount, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
        this.usedAmount = usedAmount;
        this.canceledAmount = canceledAmount;
        this.createdAt = createdAt;
    }

    public static PointUsageDetail create(UUID transactionId, UUID ledgerId, Long usedAmount) {
        return PointUsageDetail.builder()
                .id(UuidGenerator.generate())
                .transactionId(transactionId)
                .ledgerId(ledgerId)
                .usedAmount(usedAmount)
                .canceledAmount(0L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Long getCancelableAmount() {
        return usedAmount - canceledAmount;
    }

    public Long cancel(Long amount) {
        Long cancelAmount = Math.min(amount, getCancelableAmount());
        this.canceledAmount += cancelAmount;
        return cancelAmount;
    }

    public boolean isCancelable() {
        return getCancelableAmount() > 0;
    }
}
