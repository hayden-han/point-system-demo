package com.musinsa.pointsystem.infra.config;

import com.musinsa.pointsystem.domain.service.PointEarnValidator;
import com.musinsa.pointsystem.domain.service.PointRestorePolicy;
import com.musinsa.pointsystem.domain.service.PointUsagePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public PointEarnValidator pointEarnValidator() {
        return new PointEarnValidator();
    }

    @Bean
    public PointUsagePolicy pointUsagePolicy() {
        return new PointUsagePolicy();
    }

    @Bean
    public PointRestorePolicy pointRestorePolicy() {
        return new PointRestorePolicy();
    }
}
