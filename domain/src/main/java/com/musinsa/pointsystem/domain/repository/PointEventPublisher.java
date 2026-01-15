package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.event.PointEvent;

/**
 * 포인트 이벤트 발행 포트
 * - 도메인 이벤트를 외부 시스템에 발행
 */
public interface PointEventPublisher {

    /**
     * 이벤트 발행
     */
    void publish(PointEvent event);
}
