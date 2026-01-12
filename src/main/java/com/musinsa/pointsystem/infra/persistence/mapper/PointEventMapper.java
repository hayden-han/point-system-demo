package com.musinsa.pointsystem.infra.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.musinsa.pointsystem.domain.event.*;
import com.musinsa.pointsystem.infra.persistence.entity.PointEventEntity;
import org.springframework.stereotype.Component;

/**
 * 포인트 이벤트 매퍼
 * - Domain Event ↔ Entity 변환
 * - JSON 직렬화/역직렬화
 */
@Component
public class PointEventMapper {

    private final ObjectMapper objectMapper;

    public PointEventMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Domain Event → Entity 변환
     */
    public PointEventEntity toEntity(PointEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            return PointEventEntity.builder()
                    .id(event.getEventId())
                    .aggregateId(event.getAggregateId())
                    .eventType(event.getEventType())
                    .eventData(eventData)
                    .version(event.getVersion())
                    .occurredAt(event.getOccurredAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new EventSerializationException("Failed to serialize event: " + event.getEventType(), e);
        }
    }

    /**
     * Entity → Domain Event 변환
     */
    public PointEvent toDomain(PointEventEntity entity) {
        try {
            Class<? extends PointEvent> eventClass = getEventClass(entity.getEventType());
            return objectMapper.readValue(entity.getEventData(), eventClass);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(
                    "Failed to deserialize event: " + entity.getEventType(), e);
        }
    }

    /**
     * 이벤트 타입 문자열 → 클래스 매핑
     */
    private Class<? extends PointEvent> getEventClass(String eventType) {
        return switch (eventType) {
            case PointEarnedEvent.EVENT_TYPE -> PointEarnedEvent.class;
            case PointEarnCanceledEvent.EVENT_TYPE -> PointEarnCanceledEvent.class;
            case PointUsedEvent.EVENT_TYPE -> PointUsedEvent.class;
            case PointUseCanceledEvent.EVENT_TYPE -> PointUseCanceledEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    /**
     * 이벤트 직렬화 예외
     */
    public static class EventSerializationException extends RuntimeException {
        public EventSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
