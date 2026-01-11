package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransactionEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID memberId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "related_transaction_id", columnDefinition = "BINARY(16)")
    private UUID relatedTransactionId;

    @Column(name = "ledger_id", columnDefinition = "BINARY(16)")
    private UUID ledgerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointTransactionEntity(UUID id, UUID memberId, String type, Long amount,
                                   String orderId, UUID relatedTransactionId, UUID ledgerId) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.relatedTransactionId = relatedTransactionId;
        this.ledgerId = ledgerId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
