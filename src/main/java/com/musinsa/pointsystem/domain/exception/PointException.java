package com.musinsa.pointsystem.domain.exception;

public abstract class PointException extends RuntimeException {

    public PointException(String message) {
        super(message);
    }
}
