package com.musinsa.pointsystem.domain.exception;

/**
 * 분산락 획득 실패 예외
 * - 락 획득에 실패했을 때 발생
 * - Infra 계층에서 발생하지만, Application/Domain 계층에서 처리 가능하도록 domain에 정의
 */
public class LockAcquisitionFailedException extends RuntimeException {

    public LockAcquisitionFailedException(String message) {
        super(message);
    }

    public LockAcquisitionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
