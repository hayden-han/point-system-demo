package com.musinsa.pointsystem.infra.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.musinsa.pointsystem.domain.model.MemberPointSnapshot;
import com.musinsa.pointsystem.infra.persistence.entity.PointSnapshotEntity;
import org.springframework.stereotype.Component;

/**
 * 포인트 스냅샷 매퍼
 * - Domain Snapshot ↔ Entity 변환
 * - JSON 직렬화/역직렬화
 */
@Component
public class PointSnapshotMapper {

    private final ObjectMapper objectMapper;

    public PointSnapshotMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Domain Snapshot → Entity 변환
     */
    public PointSnapshotEntity toEntity(MemberPointSnapshot snapshot) {
        try {
            String snapshotData = objectMapper.writeValueAsString(snapshot);
            return PointSnapshotEntity.builder()
                    .aggregateId(snapshot.memberId())
                    .snapshotData(snapshotData)
                    .version(snapshot.version())
                    .createdAt(snapshot.createdAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new SnapshotSerializationException(
                    "Failed to serialize snapshot for member: " + snapshot.memberId(), e);
        }
    }

    /**
     * Entity → Domain Snapshot 변환
     */
    public MemberPointSnapshot toDomain(PointSnapshotEntity entity) {
        try {
            return objectMapper.readValue(entity.getSnapshotData(), MemberPointSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new SnapshotSerializationException(
                    "Failed to deserialize snapshot for member: " + entity.getAggregateId(), e);
        }
    }

    /**
     * 스냅샷 직렬화 예외
     */
    public static class SnapshotSerializationException extends RuntimeException {
        public SnapshotSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
