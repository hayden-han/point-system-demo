package com.musinsa.pointsystem.infra.adapter;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.musinsa.pointsystem.domain.repository.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * UUID v7 생성기
 *
 * - 시간 기반 정렬 가능한 UUID 생성 (인덱스 성능 최적화)
 * - 분산 시스템에서 충돌 없는 고유 식별자 생성
 * - IdGenerator 인터페이스 구현
 */
@Component
public class UuidGenerator implements IdGenerator {

    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    @Override
    public UUID generate() {
        return GENERATOR.generate();
    }
}
