package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointTransactionEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<PointTransaction> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<PointTransaction> findByMemberId(UUID memberId, PageRequest pageRequest) {
        // 도메인 PageRequest → Spring Pageable 변환
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.pageNumber(),
                pageRequest.pageSize()
        );

        Page<PointTransactionEntity> page = jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        // Spring Page → 도메인 PageResult 변환
        List<PointTransaction> content = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return PageResult.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
