package com.musinsa.pointsystem.domain.exception;

public class InsufficientPointException extends PointException {

    public InsufficientPointException(Long required, Long available) {
        super("포인트가 부족합니다. 필요: " + required + ", 보유: " + available);
    }
}
