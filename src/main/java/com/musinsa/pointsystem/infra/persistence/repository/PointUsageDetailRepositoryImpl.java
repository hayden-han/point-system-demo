package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.infra.persistence.entity.PointUsageDetailEntity;
import com.musinsa.pointsystem.infra.persistence.mapper.PointUsageDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointUsageDetailRepositoryImpl implements PointUsageDetailRepository {

    private final PointUsageDetailJpaRepository jpaRepository;
    private final PointUsageDetailMapper mapper;

    @Override
    public PointUsageDetail save(PointUsageDetail pointUsageDetail) {
        if (pointUsageDetail.getId() != null) {
            // UUIDv7을 사용하므로 ID가 항상 존재함. DB 조회로 신규/기존 구분
            Optional<PointUsageDetailEntity> existingEntity = jpaRepository.findById(pointUsageDetail.getId());
            if (existingEntity.isPresent()) {
                // 기존 엔티티 업데이트
                PointUsageDetailEntity entity = existingEntity.get();
                entity.addCanceledAmount(pointUsageDetail.getCanceledAmount() - entity.getCanceledAmount());
                return mapper.toDomain(jpaRepository.save(entity));
            }
        }
        // 신규 엔티티 저장
        PointUsageDetailEntity entity = mapper.toEntity(pointUsageDetail);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<PointUsageDetail> saveAll(List<PointUsageDetail> pointUsageDetails) {
        if (pointUsageDetails.isEmpty()) {
            return List.of();
        }

        // ID가 이미 도메인에서 생성되므로 기존 엔티티인지 DB 조회로 확인
        List<UUID> ids = pointUsageDetails.stream()
                .map(PointUsageDetail::getId)
                .toList();

        // 기존 엔티티 조회
        Map<UUID, PointUsageDetailEntity> existingEntityMap = jpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PointUsageDetailEntity::getId, entity -> entity));

        List<PointUsageDetail> result = new java.util.ArrayList<>();
        List<PointUsageDetailEntity> newEntities = new java.util.ArrayList<>();

        for (PointUsageDetail detail : pointUsageDetails) {
            PointUsageDetailEntity existingEntity = existingEntityMap.get(detail.getId());
            if (existingEntity != null) {
                // 기존 엔티티 업데이트 - 취소 금액 차이만큼 추가
                existingEntity.addCanceledAmount(detail.getCanceledAmount() - existingEntity.getCanceledAmount());
            } else {
                // 신규 엔티티
                newEntities.add(mapper.toEntity(detail));
            }
        }

        // 신규 엔티티 배치 저장
        if (!newEntities.isEmpty()) {
            List<PointUsageDetailEntity> savedEntities = jpaRepository.saveAll(newEntities);
            result.addAll(savedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        // 기존 엔티티 배치 저장 (변경감지로 업데이트됨)
        if (!existingEntityMap.isEmpty()) {
            List<PointUsageDetailEntity> updatedEntities = jpaRepository.saveAll(existingEntityMap.values().stream().toList());
            result.addAll(updatedEntities.stream()
                    .map(mapper::toDomain)
                    .toList());
        }

        return result;
    }

    @Override
    public List<PointUsageDetail> findByTransactionId(UUID transactionId) {
        return jpaRepository.findByTransactionId(transactionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointUsageDetail> findCancelableByTransactionId(UUID transactionId) {
        return jpaRepository.findCancelableByTransactionId(transactionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
