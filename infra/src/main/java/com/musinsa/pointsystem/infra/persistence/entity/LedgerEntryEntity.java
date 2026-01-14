package com.musinsa.pointsystem.infra.persistence.entity;

import com.musinsa.pointsystem.domain.model.EntryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 적립건 변동 이력 엔티티 (entries 기반)
 */
@Entity
@Table(name = "ledger_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntryEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "ledger_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ledgerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private EntryType type;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public LedgerEntryEntity(UUID id, UUID ledgerId, EntryType type, Long amount,
                              String orderId, LocalDateTime createdAt) {
        this.id = id;
        this.ledgerId = ledgerId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }
}
