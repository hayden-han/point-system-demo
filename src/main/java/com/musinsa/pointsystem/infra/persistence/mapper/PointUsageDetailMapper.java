package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import org.springframework.stereotype.Component;

@Component
public class PointUsageDetailMapper {

    public PointUsageDetail toDomain(PointUsageDetailEntity entity) {
        return PointUsageDetail.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .ledgerId(entity.getLedgerId())
                .usedAmount(PointAmount.of(entity.getUsedAmount()))
                .canceledAmount(PointAmount.of(entity.getCanceledAmount()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PointUsageDetailEntity toEntity(PointUsageDetail domain) {
        return PointUsageDetailEntity.builder()
                .id(domain.getId())
                .transactionId(domain.getTransactionId())
                .ledgerId(domain.getLedgerId())
                .usedAmount(domain.getUsedAmount().getValue())
                .canceledAmount(domain.getCanceledAmount().getValue())
                .build();
    }
}
