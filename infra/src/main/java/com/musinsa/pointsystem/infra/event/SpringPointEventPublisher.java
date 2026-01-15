package com.musinsa.pointsystem.infra.event;

import com.musinsa.pointsystem.domain.event.PointEvent;
import com.musinsa.pointsystem.domain.repository.PointEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationEventPublisher 기반 이벤트 발행 구현체
 * - 동일 트랜잭션 내에서 이벤트 발행
 * - 추후 Kafka/RabbitMQ 등으로 교체 가능
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringPointEventPublisher implements PointEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PointEvent event) {
        log.debug("도메인 이벤트 발행. eventType={}, memberId={}, amount={}",
                event.getClass().getSimpleName(), event.memberId(), event.amount());
        applicationEventPublisher.publishEvent(event);
    }
}
