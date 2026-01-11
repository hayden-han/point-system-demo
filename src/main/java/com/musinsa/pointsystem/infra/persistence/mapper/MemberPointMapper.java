package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberPointMapper {

    public MemberPoint toDomain(MemberPointEntity entity) {
        return MemberPoint.builder()
                .memberId(entity.getMemberId())
                .totalBalance(PointAmount.of(entity.getTotalBalance()))
                .build();
    }

    public MemberPointEntity toEntity(MemberPoint domain) {
        return new MemberPointEntity(domain.getMemberId(), domain.getTotalBalance().getValue());
    }
}
