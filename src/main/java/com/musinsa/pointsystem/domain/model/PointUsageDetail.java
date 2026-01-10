package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointUsageDetail {
    private final Long id;
    private final Long transactionId;
    private final Long ledgerId;
    private final Long usedAmount;
    private Long canceledAmount;
    private final LocalDateTime createdAt;

    @Builder
    public PointUsageDetail(Long id, Long transactionId, Long ledgerId,
                            Long usedAmount, Long canceledAmount, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
        this.usedAmount = usedAmount;
        this.canceledAmount = canceledAmount;
        this.createdAt = createdAt;
    }

    public static PointUsageDetail create(Long transactionId, Long ledgerId, Long usedAmount) {
        return PointUsageDetail.builder()
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
