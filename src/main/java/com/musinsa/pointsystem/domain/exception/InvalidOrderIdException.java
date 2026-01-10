package com.musinsa.pointsystem.domain.exception;

public class InvalidOrderIdException extends PointException {

    public InvalidOrderIdException() {
        super("주문번호는 필수입니다.");
    }

    public InvalidOrderIdException(String message) {
        super(message);
    }
}
