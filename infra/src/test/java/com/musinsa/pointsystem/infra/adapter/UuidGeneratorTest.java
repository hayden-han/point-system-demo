package com.musinsa.pointsystem.infra.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidGenerator 테스트")
class UuidGeneratorTest {

    private UuidGenerator uuidGenerator;

    @BeforeEach
    void setUp() {
        uuidGenerator = new UuidGenerator();
    }

    @Test
    @DisplayName("UUID 생성 - null이 아닌 UUID 반환")
    void shouldGenerateNonNullUuid() {
        UUID uuid = uuidGenerator.generate();

        assertThat(uuid).isNotNull();
    }

    @Test
    @DisplayName("UUID 생성 - 매번 고유한 값 반환")
    void shouldGenerateUniqueUuids() {
        Set<UUID> uuids = new HashSet<>();
        int count = 1000;

        for (int i = 0; i < count; i++) {
            uuids.add(uuidGenerator.generate());
        }

        assertThat(uuids).hasSize(count);
    }

    @Test
    @DisplayName("UUID v7 - 시간순 정렬 가능")
    void shouldGenerateTimeOrderedUuids() throws InterruptedException {
        UUID first = uuidGenerator.generate();
        Thread.sleep(1);
        UUID second = uuidGenerator.generate();
        Thread.sleep(1);
        UUID third = uuidGenerator.generate();

        // UUID v7은 시간 기반이므로 문자열 비교 시 순서 유지
        assertThat(first.toString().compareTo(second.toString())).isLessThan(0);
        assertThat(second.toString().compareTo(third.toString())).isLessThan(0);
    }
}
