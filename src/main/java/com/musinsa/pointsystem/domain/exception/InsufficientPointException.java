package com.musinsa.pointsystem.domain.exception;

public class InsufficientPointException extends PointException {

    private static final String USER_MESSAGE = "포인트가 부족합니다.";

    public InsufficientPointException(Long required, Long available) {
        super(USER_MESSAGE,
              String.format("포인트 부족. 필요: %d, 보유: %d", required, available));
    }
}
