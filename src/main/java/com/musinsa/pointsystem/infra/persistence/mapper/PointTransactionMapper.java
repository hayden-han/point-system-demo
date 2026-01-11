package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.OrderId;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.model.TransactionType;
import com.musinsa.pointsystem.infra.persistence.entity.PointTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class PointTransactionMapper {

    public PointTransaction toDomain(PointTransactionEntity entity) {
        return PointTransaction.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .type(TransactionType.valueOf(entity.getType()))
                .amount(PointAmount.of(entity.getAmount()))
                .orderId(entity.getOrderId() != null ? OrderId.of(entity.getOrderId()) : null)
                .relatedTransactionId(entity.getRelatedTransactionId())
                .ledgerId(entity.getLedgerId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PointTransactionEntity toEntity(PointTransaction domain) {
        return PointTransactionEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .type(domain.getType().name())
                .amount(domain.getAmount().getValue())
                .orderId(domain.getOrderId() != null ? domain.getOrderId().getValue() : null)
                .relatedTransactionId(domain.getRelatedTransactionId())
                .ledgerId(domain.getLedgerId())
                .build();
    }
}
