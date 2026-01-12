package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.stereotype.Component;

@Component
public class PointLedgerMapper {

    public PointLedger toDomain(PointLedgerEntity entity) {
        return new PointLedger(
                entity.getId(),
                entity.getMemberId(),
                PointAmount.of(entity.getEarnedAmount()),
                PointAmount.of(entity.getAvailableAmount()),
                PointAmount.of(entity.getUsedAmount()),
                EarnType.valueOf(entity.getEarnType()),
                entity.getSourceTransactionId(),
                entity.getExpiredAt(),
                entity.getIsCanceled(),
                entity.getEarnedAt()
        );
    }

    public PointLedgerEntity toEntity(PointLedger domain) {
        return PointLedgerEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .earnedAmount(domain.getEarnedAmount().getValue())
                .availableAmount(domain.getAvailableAmount().getValue())
                .usedAmount(domain.getUsedAmount().getValue())
                .earnType(domain.getEarnType().name())
                .sourceTransactionId(domain.getSourceTransactionId())
                .expiredAt(domain.getExpiredAt())
                .isCanceled(domain.isCanceled())
                .earnedAt(domain.getEarnedAt())
                .build();
    }
}
