package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "point_usage_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetailEntity extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "transaction_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID transactionId;

    @Column(name = "ledger_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ledgerId;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Column(name = "canceled_amount", nullable = false)
    private Long canceledAmount;

    @Builder
    public PointUsageDetailEntity(UUID id, UUID transactionId, UUID ledgerId, Long usedAmount, Long canceledAmount) {
        this.id = id;
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
        this.usedAmount = usedAmount;
        this.canceledAmount = canceledAmount;
    }

    public void addCanceledAmount(Long amount) {
        this.canceledAmount += amount;
    }
}
