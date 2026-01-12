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
        return new PointTransaction(
                entity.getId(),
                entity.getMemberId(),
                TransactionType.valueOf(entity.getType()),
                PointAmount.of(entity.getAmount()),
                entity.getOrderId() != null ? OrderId.of(entity.getOrderId()) : null,
                entity.getRelatedTransactionId(),
                entity.getLedgerId(),
                entity.getTransactedAt()
        );
    }

    public PointTransactionEntity toEntity(PointTransaction domain) {
        return PointTransactionEntity.builder()
                .id(domain.id())
                .memberId(domain.memberId())
                .type(domain.type().name())
                .amount(domain.amount().value())
                .orderId(domain.orderId() != null ? domain.orderId().value() : null)
                .relatedTransactionId(domain.relatedTransactionId())
                .ledgerId(domain.ledgerId())
                .transactedAt(domain.transactedAt())
                .build();
    }
}
