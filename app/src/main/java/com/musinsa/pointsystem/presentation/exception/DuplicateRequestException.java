package com.musinsa.pointsystem.presentation.exception;

public class DuplicateRequestException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateRequestException(String idempotencyKey) {
        super("중복된 요청입니다. Idempotency-Key: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
