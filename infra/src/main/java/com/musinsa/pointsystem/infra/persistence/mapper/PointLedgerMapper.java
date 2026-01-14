package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PointLedgerMapper {

    /**
     * Entity → Domain (entries 없이 - 레거시 호환)
     */
    public PointLedger toDomain(PointLedgerEntity entity) {
        return toDomain(entity, Collections.emptyList());
    }

    /**
     * Entity → Domain (entries 포함 - entries 기반)
     */
    public PointLedger toDomain(PointLedgerEntity entity, List<LedgerEntryEntity> entryEntities) {
        List<LedgerEntry> entries = entryEntities.stream()
                .map(this::toEntryDomain)
                .toList();

        return new PointLedger(
                entity.getId(),
                entity.getMemberId(),
                PointAmount.of(entity.getEarnedAmount()),
                PointAmount.of(entity.getAvailableAmount()),
                EarnType.valueOf(entity.getEarnType()),
                entity.getSourceLedgerId(),
                entity.getExpiredAt(),
                entity.getIsCanceled(),
                entity.getEarnedAt(),
                entries
        );
    }

    public PointLedgerEntity toEntity(PointLedger domain) {
        return PointLedgerEntity.builder()
                .id(domain.id())
                .memberId(domain.memberId())
                .earnedAmount(domain.earnedAmount().value())
                .availableAmount(domain.availableAmount().value())
                .usedAmount(domain.usedAmount().value())  // entries 기반 계산값
                .earnType(domain.earnType().name())
                .sourceLedgerId(domain.sourceLedgerId())
                .expiredAt(domain.expiredAt())
                .isCanceled(domain.canceled())
                .earnedAt(domain.earnedAt())
                .build();
    }

    /**
     * LedgerEntryEntity → LedgerEntry
     */
    public LedgerEntry toEntryDomain(LedgerEntryEntity entity) {
        return new LedgerEntry(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getOrderId(),
                entity.getCreatedAt()
        );
    }

    /**
     * LedgerEntry → LedgerEntryEntity
     */
    public LedgerEntryEntity toEntryEntity(LedgerEntry domain, java.util.UUID ledgerId) {
        return LedgerEntryEntity.builder()
                .id(domain.id())
                .ledgerId(ledgerId)
                .type(domain.type())
                .amount(domain.amount())
                .orderId(domain.orderId())
                .createdAt(domain.createdAt())
                .build();
    }
}
