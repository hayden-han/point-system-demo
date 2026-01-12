package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 포인트 거래 이력 엔티티 (Append-only)
 * - updatedAt 없음: 생성 후 수정되지 않음
 */
@Entity
@Table(name = "point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransactionEntity extends BaseTimeEntity {

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
}
