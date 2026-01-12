package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLedgerEntity extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID memberId;

    @Column(name = "earned_amount", nullable = false)
    private Long earnedAmount;

    @Column(name = "available_amount", nullable = false)
    private Long availableAmount;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Column(name = "earn_type", nullable = false, length = 20)
    private String earnType;

    @Column(name = "source_transaction_id", columnDefinition = "BINARY(16)")
    private UUID sourceTransactionId;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_canceled", nullable = false)
    private Boolean isCanceled;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Builder
    public PointLedgerEntity(UUID id, UUID memberId, Long earnedAmount, Long availableAmount,
                             Long usedAmount, String earnType, UUID sourceTransactionId,
                             LocalDateTime expiredAt, Boolean isCanceled, LocalDateTime earnedAt) {
        this.id = id;
        this.memberId = memberId;
        this.earnedAmount = earnedAmount;
        this.availableAmount = availableAmount;
        this.usedAmount = usedAmount;
        this.earnType = earnType;
        this.sourceTransactionId = sourceTransactionId;
        this.expiredAt = expiredAt;
        this.isCanceled = isCanceled;
        this.earnedAt = earnedAt;
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
}
