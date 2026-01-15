package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PointLedgerMapper {

    // =====================================================
    // PointLedger 변환
    // =====================================================

    public PointLedger toDomain(PointLedgerEntity entity) {
        return new PointLedger(
                entity.getId(),
                entity.getMemberId(),
                entity.getEarnedAmount(),
                entity.getAvailableAmount(),
                EarnType.valueOf(entity.getEarnType()),
                entity.getSourceLedgerId(),
                entity.getExpiredAt(),
                entity.getIsCanceled(),
                entity.getEarnedAt()
        );
    }

    public PointLedgerEntity toEntity(PointLedger domain) {
        return PointLedgerEntity.builder()
                .id(domain.id())
                .memberId(domain.memberId())
                .earnedAmount(domain.earnedAmount())
                .availableAmount(domain.availableAmount())
                .usedAmount(domain.earnedAmount() - domain.availableAmount())
                .earnType(domain.earnType().name())
                .sourceLedgerId(domain.sourceLedgerId())
                .expiredAt(domain.expiredAt())
                .isCanceled(domain.canceled())
                .earnedAt(domain.earnedAt())
                .build();
    }

    // =====================================================
    // LedgerEntry 변환
    // =====================================================

    public LedgerEntry toEntryDomain(LedgerEntryEntity entity) {
        return new LedgerEntry(
                entity.getId(),
                entity.getLedgerId(),
                entity.getType(),
                entity.getAmount(),
                entity.getOrderId(),
                entity.getCreatedAt()
        );
    }

    public LedgerEntryEntity toEntryEntity(LedgerEntry domain) {
        return LedgerEntryEntity.builder()
                .id(domain.id())
                .ledgerId(domain.ledgerId())
                .type(domain.type())
                .amount(domain.amount())
                .orderId(domain.orderId())
                .createdAt(domain.createdAt())
                .build();
    }
}
