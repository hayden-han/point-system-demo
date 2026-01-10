package com.musinsa.pointsystem.domain.exception;

public class InvalidCancelAmountException extends PointException {

    public InvalidCancelAmountException(Long requested, Long cancelable) {
        super("취소 가능 금액을 초과했습니다. 요청: " + requested + ", 취소가능: " + cancelable);
    }
}
