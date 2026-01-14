package com.musinsa.pointsystem.domain.port;

import java.util.UUID;

/**
 * ID 생성 포트 (도메인 레이어)
 * - 도메인이 인프라에 의존하지 않도록 추상화
 * - 구현체는 인프라 레이어에서 제공
 */
public interface IdGenerator {

    /**
     * 고유 ID 생성
     *
     * @return 새로운 UUID
     */
    UUID generate();
}
