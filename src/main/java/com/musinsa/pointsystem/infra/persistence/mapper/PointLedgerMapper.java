package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.springframework.stereotype.Component;

@Component
public class PointLedgerMapper {

    public PointLedger toDomain(PointLedgerEntity entity) {
        return PointLedger.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .earnedAmount(entity.getEarnedAmount())
                .availableAmount(entity.getAvailableAmount())
                .usedAmount(entity.getUsedAmount())
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
                .earnedAmount(domain.getEarnedAmount())
                .availableAmount(domain.getAvailableAmount())
                .usedAmount(domain.getUsedAmount())
                .earnType(domain.getEarnType().name())
                .sourceTransactionId(domain.getSourceTransactionId())
                .expiredAt(domain.getExpiredAt())
                .isCanceled(domain.isCanceled())
                .build();
    }
}
