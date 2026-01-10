package com.musinsa.pointsystem.domain.exception;

public class InvalidEarnAmountException extends PointException {

    public InvalidEarnAmountException(String message) {
        super(message);
    }

    public static InvalidEarnAmountException belowMinimum(Long amount, Long minAmount) {
        return new InvalidEarnAmountException(
                "최소 적립 금액보다 작습니다. 요청: " + amount + ", 최소: " + minAmount);
    }

    public static InvalidEarnAmountException aboveMaximum(Long amount, Long maxAmount) {
        return new InvalidEarnAmountException(
                "최대 적립 금액을 초과했습니다. 요청: " + amount + ", 최대: " + maxAmount);
    }
}
