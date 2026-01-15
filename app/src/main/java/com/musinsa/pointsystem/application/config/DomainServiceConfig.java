package com.musinsa.pointsystem.application.config;

import com.musinsa.pointsystem.domain.repository.IdGenerator;
import com.musinsa.pointsystem.domain.service.UseCancelProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 서비스 Bean 등록
 *
 * domain 모듈은 순수 Java로 Spring 의존성이 없으므로
 * Application 레이어에서 Bean으로 등록합니다.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public UseCancelProcessor useCancelProcessor(IdGenerator idGenerator) {
        return new UseCancelProcessor(idGenerator);
    }
}
