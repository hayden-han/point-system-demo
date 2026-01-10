package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_usage_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Column(name = "canceled_amount", nullable = false)
    private Long canceledAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public PointUsageDetailEntity(Long transactionId, Long ledgerId, Long usedAmount, Long canceledAmount) {
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
        this.usedAmount = usedAmount;
        this.canceledAmount = canceledAmount;
    }

    public void addCanceledAmount(Long amount) {
        this.canceledAmount += amount;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
