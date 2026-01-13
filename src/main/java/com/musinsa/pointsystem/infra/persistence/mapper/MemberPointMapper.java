package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberPointMapper {

    private final PointLedgerMapper pointLedgerMapper;

    /**
     * Entity → Domain (ledgers 없이)
     * @deprecated v2에서는 totalBalance가 조회 시 계산되므로 ledgers 포함 버전 사용 권장
     */
    @Deprecated
    public MemberPoint toDomain(MemberPointEntity entity) {
        return MemberPoint.of(
                entity.getMemberId(),
                List.of()
        );
    }

    /**
     * Entity → Domain (Fetch Join으로 로드된 ledgers 포함)
     */
    public MemberPoint toDomainWithLedgers(MemberPointEntity entity) {
        List<PointLedger> ledgers = entity.getLedgers() != null
                ? entity.getLedgers().stream().map(pointLedgerMapper::toDomain).toList()
                : List.of();

        return MemberPoint.of(entity.getMemberId(), ledgers);
    }

    /**
     * Entity → Domain (외부에서 전달된 ledgers 포함)
     */
    public MemberPoint toDomainWithLedgers(MemberPointEntity entity, List<PointLedgerEntity> ledgerEntities) {
        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(pointLedgerMapper::toDomain)
                .toList();

        return MemberPoint.of(entity.getMemberId(), ledgers);
    }

    /**
     * Ledger 엔티티 목록만으로 MemberPoint 생성 (v2)
     */
    public MemberPoint toDomainFromLedgers(java.util.UUID memberId, List<PointLedgerEntity> ledgerEntities) {
        List<PointLedger> ledgers = ledgerEntities.stream()
                .map(pointLedgerMapper::toDomain)
                .toList();

        return MemberPoint.of(memberId, ledgers);
    }

    /**
     * @deprecated v2에서는 member_point 테이블 제거 예정
     */
    @Deprecated
    public MemberPointEntity toEntity(MemberPoint domain) {
        return new MemberPointEntity(domain.memberId(), domain.totalBalance().value());
    }
}
