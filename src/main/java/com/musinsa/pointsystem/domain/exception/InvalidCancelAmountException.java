package com.musinsa.pointsystem.domain.exception;

public class InvalidCancelAmountException extends PointException {

    private static final String USER_MESSAGE = "취소 가능 금액을 초과했습니다.";

    public InvalidCancelAmountException(Long requested, Long cancelable) {
        super(USER_MESSAGE,
              String.format("취소 가능 금액 초과. 요청: %d, 취소가능: %d", requested, cancelable));
    }
}
