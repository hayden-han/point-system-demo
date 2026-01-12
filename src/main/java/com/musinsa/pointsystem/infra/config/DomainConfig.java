package com.musinsa.pointsystem.infra.config;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.factory.PointFactory;
import com.musinsa.pointsystem.domain.port.IdGenerator;
import com.musinsa.pointsystem.domain.service.PointAccrualManager;
import com.musinsa.pointsystem.domain.service.PointUsageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 계층 빈 설정
 * - 도메인 계층의 프레임워크 독립성을 위해 인프라 계층에서 빈 등록
 * - PointFactory, PointAccrualManager, PointUsageManager는 Spring 어노테이션 없이 순수 Java 클래스로 유지
 */
@Configuration
public class DomainConfig {

    @Bean
    public PointFactory pointFactory(IdGenerator idGenerator, TimeProvider timeProvider) {
        return new PointFactory(idGenerator, timeProvider);
    }

    @Bean
    public PointAccrualManager pointAccrualManager(PointFactory pointFactory) {
        return new PointAccrualManager(pointFactory);
    }

    @Bean
    public PointUsageManager pointUsageManager(PointFactory pointFactory, TimeProvider timeProvider) {
        return new PointUsageManager(pointFactory, timeProvider);
    }
}
