package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.MemberPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberPointRepositoryImpl implements MemberPointRepository {

    private final MemberPointJpaRepository jpaRepository;
    private final MemberPointMapper mapper;

    @Override
    public Optional<MemberPoint> findByMemberId(Long memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain);
    }

    @Override
    public MemberPoint save(MemberPoint memberPoint) {
        MemberPointEntity entity = jpaRepository.findById(memberPoint.getMemberId())
                .map(existing -> {
                    existing.updateTotalBalance(memberPoint.getTotalBalance());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(memberPoint));

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public MemberPoint getOrCreate(Long memberId) {
        return jpaRepository.findById(memberId)
                .map(mapper::toDomain)
                .orElseGet(() -> {
                    MemberPoint newMemberPoint = MemberPoint.create(memberId);
                    MemberPointEntity entity = mapper.toEntity(newMemberPoint);
                    return mapper.toDomain(jpaRepository.save(entity));
                });
    }
}
