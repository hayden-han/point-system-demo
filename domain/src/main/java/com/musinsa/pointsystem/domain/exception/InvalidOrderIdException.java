package com.musinsa.pointsystem.domain.exception;

public class InvalidOrderIdException extends PointException {

    private static final String USER_MESSAGE = "주문번호는 필수입니다.";

    public InvalidOrderIdException() {
        super(USER_MESSAGE);
    }

    public InvalidOrderIdException(String internalDetail) {
        super(USER_MESSAGE, internalDetail);
    }
}
