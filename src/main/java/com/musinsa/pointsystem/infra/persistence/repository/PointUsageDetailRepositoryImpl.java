package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointUsageDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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
        if (pointUsageDetails.isEmpty()) {
            return List.of();
        }

        // 신규 저장과 업데이트 분리
        List<PointUsageDetail> newDetails = pointUsageDetails.stream()
                .filter(detail -> detail.getId() == null)
                .toList();
        List<PointUsageDetail> existingDetails = pointUsageDetails.stream()
                .filter(detail -> detail.getId() != null)
                .toList();

        List<PointUsageDetail> result = new java.util.ArrayList<>();

        // 신규 사용상세 배치 저장
        if (!newDetails.isEmpty()) {
            List<PointUsageDetailEntity> newEntities = newDetails.stream()
                    .map(mapper::toEntity)
                    .toList();
            List<PointUsageDetailEntity> savedEntities = jpaRepository.saveAll(newEntities);
            result.addAll(savedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        // 기존 사용상세 배치 업데이트 (한 번의 쿼리로 모든 엔티티 조회 후 업데이트)
        if (!existingDetails.isEmpty()) {
            List<Long> ids = existingDetails.stream()
                    .map(PointUsageDetail::getId)
                    .toList();

            // 한 번의 쿼리로 모든 기존 엔티티 조회
            Map<Long, PointUsageDetailEntity> entityMap = jpaRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(PointUsageDetailEntity::getId, entity -> entity));

            for (PointUsageDetail detail : existingDetails) {
                PointUsageDetailEntity entity = entityMap.get(detail.getId());
                if (entity == null) {
                    throw new IllegalArgumentException("사용 상세를 찾을 수 없습니다: " + detail.getId());
                }
                // 취소 금액 차이만큼 추가
                entity.addCanceledAmount(detail.getCanceledAmount() - entity.getCanceledAmount());
            }

            // 변경 감지에 의해 자동으로 업데이트됨 (또는 명시적 saveAll)
            List<PointUsageDetailEntity> updatedEntities = jpaRepository.saveAll(entityMap.values().stream().toList());
            result.addAll(updatedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        return result;
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
