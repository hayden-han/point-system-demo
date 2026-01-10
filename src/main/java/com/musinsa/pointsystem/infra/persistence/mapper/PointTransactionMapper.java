package com.musinsa.pointsystem.infra.persistence.mapper;

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
                .amount(entity.getAmount())
                .orderId(entity.getOrderId())
                .relatedTransactionId(entity.getRelatedTransactionId())
                .ledgerId(entity.getLedgerId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PointTransactionEntity toEntity(PointTransaction domain) {
        return PointTransactionEntity.builder()
                .memberId(domain.getMemberId())
                .type(domain.getType().name())
                .amount(domain.getAmount())
                .orderId(domain.getOrderId())
                .relatedTransactionId(domain.getRelatedTransactionId())
                .ledgerId(domain.getLedgerId())
                .build();
    }
}
