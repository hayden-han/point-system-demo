package com.musinsa.pointsystem.domain.infrastructure;

import java.util.Optional;

/**
 * 멱등성 키 관리 포트
 *
 * [배치 이유]
 * 멱등성은 비즈니스 로직이 아닌 기술적 관심사(Cross-Cutting Concern)입니다.
 * 그러나 실용적인 이유로 domain 모듈에 배치했습니다:
 *
 * - 모듈 의존성 제약: app 모듈(IdempotencySupport)과 infra 모듈(IdempotencyKeyRepository)
 *   모두에서 이 인터페이스를 사용해야 하지만, infra → app 방향의 의존성은 허용되지 않음
 * - 따라서 양쪽 모듈이 공통으로 의존하는 domain 모듈에 인터페이스를 배치
 * - domain.infrastructure 패키지로 분리하여 비즈니스 도메인(model, service)과 구분
 */
public interface IdempotencyKeyPort {

    /**
     * 멱등성 키 획득 결과
     */
    enum AcquireResult {
        ACQUIRED,           // 새로 획득 (처리 가능)
        ALREADY_COMPLETED,  // 이미 처리 완료
        PROCESSING          // 다른 요청이 처리 중
    }

    /**
     * 멱등성 키가 이미 존재하는지 확인하고, 없으면 저장
     * @return AcquireResult 상태
     */
    AcquireResult tryAcquire(String idempotencyKey);

    /**
     * 처리 완료 후 결과 저장
     */
    void saveResult(String idempotencyKey, String result);

    /**
     * 저장된 결과 조회
     * @return 처리 완료된 결과 (PROCESSING 상태면 empty)
     */
    Optional<String> getResult(String idempotencyKey);

    /**
     * 처리 실패 시 키 삭제 (재시도 허용)
     */
    void remove(String idempotencyKey);
}
