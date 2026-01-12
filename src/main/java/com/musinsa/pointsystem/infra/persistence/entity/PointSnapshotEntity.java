package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 스냅샷 JPA 엔티티
 * - 이벤트 리플레이 성능 최적화를 위한 상태 저장
 * - aggregate_id당 최신 스냅샷만 유지 (upsert)
 */
@Entity
@Table(name = "point_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointSnapshotEntity {

    @Id
    @Column(name = "aggregate_id", columnDefinition = "uuid")
    private UUID aggregateId;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "text")
    private String snapshotData;

    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 스냅샷 업데이트
     */
    public void update(String snapshotData, Long version, LocalDateTime createdAt) {
        this.snapshotData = snapshotData;
        this.version = version;
        this.createdAt = createdAt;
    }
}
