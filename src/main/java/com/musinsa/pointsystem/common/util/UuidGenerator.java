package com.musinsa.pointsystem.common.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.UUID;

/**
 * UUID v7 생성 유틸리티
 *
 * - 시간 기반 정렬 가능한 UUID 생성 (인덱스 성능 최적화)
 * - 분산 시스템에서 충돌 없는 고유 식별자 생성
 */
public final class UuidGenerator {

    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    private UuidGenerator() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * UUID v7 생성
     *
     * @return 시간 기반 정렬 가능한 UUID
     */
    public static UUID generate() {
        return GENERATOR.generate();
    }
}
