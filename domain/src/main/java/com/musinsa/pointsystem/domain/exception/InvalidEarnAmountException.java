package com.musinsa.pointsystem.domain.exception;

public class InvalidEarnAmountException extends PointException {

    private static final String USER_MESSAGE_BELOW_MIN = "최소 적립 금액보다 작습니다.";
    private static final String USER_MESSAGE_ABOVE_MAX = "최대 적립 금액을 초과했습니다.";

    private InvalidEarnAmountException(String userMessage, String internalMessage) {
        super(userMessage, internalMessage);
    }

    public static InvalidEarnAmountException belowMinimum(Long amount, Long minAmount) {
        return new InvalidEarnAmountException(
                USER_MESSAGE_BELOW_MIN,
                String.format("최소 적립 금액 미달. 요청: %d, 최소: %d", amount, minAmount));
    }

    public static InvalidEarnAmountException aboveMaximum(Long amount, Long maxAmount) {
        return new InvalidEarnAmountException(
                USER_MESSAGE_ABOVE_MAX,
                String.format("최대 적립 금액 초과. 요청: %d, 최대: %d", amount, maxAmount));
    }
}
