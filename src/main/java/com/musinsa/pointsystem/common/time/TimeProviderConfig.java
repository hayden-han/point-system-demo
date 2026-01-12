package com.musinsa.pointsystem.common.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TimeProvider Bean 설정
 */
@Configuration
public class TimeProviderConfig {

    @Bean
    public TimeProvider timeProvider() {
        return TimeProvider.system();
    }
}
