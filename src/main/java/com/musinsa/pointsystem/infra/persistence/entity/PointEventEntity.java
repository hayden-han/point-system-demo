package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 이벤트 JPA 엔티티
 * - 이벤트 소싱의 이벤트 저장소
 * - Append-only (수정/삭제 불가)
 * - aggregate_id + version 유니크 제약 (낙관적 동시성 제어)
 */
@Entity
@Table(
        name = "point_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_events_aggregate_version",
                columnNames = {"aggregate_id", "version"}
        ),
        indexes = @Index(
                name = "idx_point_events_aggregate_version",
                columnList = "aggregate_id, version"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointEventEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "aggregate_id", nullable = false, columnDefinition = "uuid")
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_data", nullable = false, columnDefinition = "text")
    private String eventData;

    @Column(nullable = false)
    private Long version;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
