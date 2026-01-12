package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import org.springframework.stereotype.Component;

@Component
public class PointUsageDetailMapper {

    public PointUsageDetail toDomain(PointUsageDetailEntity entity) {
        return new PointUsageDetail(
                entity.getId(),
                entity.getTransactionId(),
                entity.getLedgerId(),
                PointAmount.of(entity.getUsedAmount()),
                PointAmount.of(entity.getCanceledAmount()),
                entity.getCreatedAt()
        );
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
