package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointUsageDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointUsageDetailRepositoryImpl implements PointUsageDetailRepository {

    private final PointUsageDetailJpaRepository jpaRepository;
    private final PointUsageDetailMapper mapper;

    @Override
    public PointUsageDetail save(PointUsageDetail pointUsageDetail) {
        if (pointUsageDetail.getId() != null) {
            PointUsageDetailEntity entity = jpaRepository.findById(pointUsageDetail.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용 상세를 찾을 수 없습니다: " + pointUsageDetail.getId()));
            entity.addCanceledAmount(pointUsageDetail.getCanceledAmount() - entity.getCanceledAmount());
            return mapper.toDomain(jpaRepository.save(entity));
        }
        PointUsageDetailEntity entity = mapper.toEntity(pointUsageDetail);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<PointUsageDetail> saveAll(List<PointUsageDetail> pointUsageDetails) {
        return pointUsageDetails.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointUsageDetail> findByTransactionId(Long transactionId) {
        return jpaRepository.findByTransactionId(transactionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointUsageDetail> findCancelableByTransactionId(Long transactionId) {
        return jpaRepository.findCancelableByTransactionId(transactionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
