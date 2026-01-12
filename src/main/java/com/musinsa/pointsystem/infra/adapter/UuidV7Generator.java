package com.musinsa.pointsystem.infra.adapter;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.musinsa.pointsystem.domain.port.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * UUID v7 생성기 구현체
 * - 시간 기반 정렬 가능한 UUID 생성 (인덱스 성능 최적화)
 * - 분산 시스템에서 충돌 없는 고유 식별자 생성
 */
@Component
public class UuidV7Generator implements IdGenerator {

    private final TimeBasedEpochGenerator generator = Generators.timeBasedEpochGenerator();

    @Override
    public UUID generate() {
        return generator.generate();
    }
}
