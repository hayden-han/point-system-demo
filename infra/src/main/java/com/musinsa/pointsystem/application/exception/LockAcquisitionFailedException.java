package com.musinsa.pointsystem.application.exception;

public class LockAcquisitionFailedException extends RuntimeException {

    public LockAcquisitionFailedException(String message) {
        super(message);
    }

    public LockAcquisitionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
