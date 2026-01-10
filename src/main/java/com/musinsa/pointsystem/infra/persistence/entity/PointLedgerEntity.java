package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "earned_amount", nullable = false)
    private Long earnedAmount;

    @Column(name = "available_amount", nullable = false)
    private Long availableAmount;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Column(name = "earn_type", nullable = false, length = 20)
    private String earnType;

    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_canceled", nullable = false)
    private Boolean isCanceled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public PointLedgerEntity(Long memberId, Long earnedAmount, Long availableAmount,
                             Long usedAmount, String earnType, Long sourceTransactionId,
                             LocalDateTime expiredAt, Boolean isCanceled) {
        this.memberId = memberId;
        this.earnedAmount = earnedAmount;
        this.availableAmount = availableAmount;
        this.usedAmount = usedAmount;
        this.earnType = earnType;
        this.sourceTransactionId = sourceTransactionId;
        this.expiredAt = expiredAt;
        this.isCanceled = isCanceled;
    }

    public void updateAvailableAmount(Long availableAmount, Long usedAmount) {
        this.availableAmount = availableAmount;
        this.usedAmount = usedAmount;
    }

    public void cancel() {
        this.isCanceled = true;
        this.availableAmount = 0L;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
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
