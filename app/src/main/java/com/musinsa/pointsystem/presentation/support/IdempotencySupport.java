package com.musinsa.pointsystem.presentation.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.pointsystem.domain.infrastructure.IdempotencyKeyPort;
import com.musinsa.pointsystem.domain.infrastructure.IdempotencyKeyPort.AcquireResult;
import com.musinsa.pointsystem.presentation.exception.DuplicateRequestException;
import com.musinsa.pointsystem.presentation.exception.RequestInProgressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 멱등성 처리 공통 로직
 * - 모든 변경 API에서 사용
 * - 중복 요청 방지
 * - 결과 캐싱
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencySupport {

    private final IdempotencyKeyPort idempotencyKeyPort;
    private final ObjectMapper objectMapper;

    /**
     * 멱등성 보장 실행 (UseCase + 변환)
     * @param idempotencyKey 멱등성 키 (null이면 멱등성 처리 없이 실행)
     * @param responseType 응답 타입
     * @param useCase UseCase 실행 함수
     * @param mapper 결과를 응답으로 변환하는 함수
     * @return 응답 객체
     */
    public <R, T> T execute(
            String idempotencyKey,
            Class<T> responseType,
            Supplier<R> useCase,
            Function<R, T> mapper) {
        return executeWithIdempotency(idempotencyKey, responseType, () -> mapper.apply(useCase.get()));
    }

    /**
     * 멱등성 보장 실행
     * @param idempotencyKey 멱등성 키 (null이면 멱등성 처리 없이 실행)
     * @param responseType 응답 타입
     * @param operation 실제 비즈니스 로직
     * @return 응답 객체
     */
    public <T> T executeWithIdempotency(String idempotencyKey, Class<T> responseType, Supplier<T> operation) {
        // 멱등성 키가 없으면 바로 실행
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return operation.get();
        }

        // 1. 이미 처리된 결과가 있으면 반환
        Optional<T> cachedResult = getCachedResult(idempotencyKey, responseType);
        if (cachedResult.isPresent()) {
            log.debug("멱등성 캐시 히트. key={}", idempotencyKey);
            return cachedResult.get();
        }

        // 2. 새 요청 처리 시도
        AcquireResult acquireResult = idempotencyKeyPort.tryAcquire(idempotencyKey);
        switch (acquireResult) {
            case ACQUIRED:
                // 정상 처리 계속
                break;
            case ALREADY_COMPLETED:
                // race condition으로 캐시 miss 후 완료된 경우
                Optional<T> completedResult = getCachedResult(idempotencyKey, responseType);
                if (completedResult.isPresent()) {
                    return completedResult.get();
                }
                throw new DuplicateRequestException(idempotencyKey);
            case PROCESSING:
                // 다른 요청이 처리 중
                throw new RequestInProgressException(idempotencyKey);
        }

        // 3. 비즈니스 로직 실행
        try {
            T response = operation.get();

            // 4. 결과 저장
            saveResult(idempotencyKey, response);

            return response;
        } catch (Exception e) {
            // 실패 시 멱등성 키 삭제 (재시도 허용)
            idempotencyKeyPort.remove(idempotencyKey);
            throw e;
        }
    }

    private <T> Optional<T> getCachedResult(String idempotencyKey, Class<T> responseType) {
        return idempotencyKeyPort.getResult(idempotencyKey)
                .flatMap(json -> {
                    try {
                        return Optional.of(objectMapper.readValue(json, responseType));
                    } catch (Exception e) {
                        log.warn("멱등성 캐시 역직렬화 실패. key={}, error={}", idempotencyKey, e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    private <T> void saveResult(String idempotencyKey, T response) {
        try {
            idempotencyKeyPort.saveResult(idempotencyKey, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.warn("멱등성 결과 저장 실패. key={}, error={}", idempotencyKey, e.getMessage());
        }
    }
}
