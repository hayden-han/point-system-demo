package com.musinsa.pointsystem.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * 도메인 계층 빈 설정
 */
@Configuration
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}
