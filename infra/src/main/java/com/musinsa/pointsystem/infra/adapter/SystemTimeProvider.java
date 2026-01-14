package com.musinsa.pointsystem.infra.adapter;

import com.musinsa.pointsystem.domain.port.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 시스템 시간 제공자 - UTC 기준
 * - TimeProvider 인터페이스 구현
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
