package com.musinsa.pointsystem.common.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 시스템 시간 제공자 - UTC 기준
 */
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
