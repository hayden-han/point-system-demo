package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointTransactionEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryImpl implements PointTransactionRepository {

    private final PointTransactionJpaRepository jpaRepository;
    private final PointTransactionMapper mapper;

    @Override
    public PointTransaction save(PointTransaction pointTransaction) {
        PointTransactionEntity entity = mapper.toEntity(pointTransaction);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PointTransaction> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<PointTransaction> findByMemberId(Long memberId, Pageable pageable) {
        return jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(mapper::toDomain);
    }
}
