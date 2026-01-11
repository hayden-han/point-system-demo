package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.stereotype.Component;

@Component
public class PointLedgerMapper {

    public PointLedger toDomain(PointLedgerEntity entity) {
        return PointLedger.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .earnedAmount(PointAmount.of(entity.getEarnedAmount()))
                .availableAmount(PointAmount.of(entity.getAvailableAmount()))
                .usedAmount(PointAmount.of(entity.getUsedAmount()))
                .earnType(EarnType.valueOf(entity.getEarnType()))
                .sourceTransactionId(entity.getSourceTransactionId())
                .expiredAt(entity.getExpiredAt())
                .isCanceled(entity.getIsCanceled())
                .createdAt(entity.getCreatedAt())
                .build();
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
                .build();
    }
}
