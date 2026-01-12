package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberPointMapper {

    private final PointLedgerMapper pointLedgerMapper;

    /**
     * Entity → Domain (ledgers 없이)
     */
    public MemberPoint toDomain(MemberPointEntity entity) {
        return MemberPoint.builder()
                .memberId(entity.getMemberId())
                .totalBalance(PointAmount.of(entity.getTotalBalance()))
                .ledgers(new ArrayList<>())
                .build();
    }

    /**
     * Entity → Domain (ledgers 포함)
     */
    public MemberPoint toDomainWithLedgers(MemberPointEntity entity, List<PointLedgerEntity> ledgerEntities) {
        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(pointLedgerMapper::toDomain)
                .toList();

        return MemberPoint.builder()
                .memberId(entity.getMemberId())
                .totalBalance(PointAmount.of(entity.getTotalBalance()))
                .ledgers(new ArrayList<>(ledgers))
                .build();
    }

    public MemberPointEntity toEntity(MemberPoint domain) {
        return new MemberPointEntity(domain.getMemberId(), domain.getTotalBalance().getValue());
    }
}
