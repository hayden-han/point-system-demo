package com.musinsa.pointsystem.presentation.exception;

/**
 * 동일한 멱등성 키로 요청이 처리 중일 때 발생
 */
public class RequestInProgressException extends RuntimeException {

    private final String idempotencyKey;

    public RequestInProgressException(String idempotencyKey) {
        super(String.format("요청이 처리 중입니다. Idempotency-Key: %s", idempotencyKey));
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
