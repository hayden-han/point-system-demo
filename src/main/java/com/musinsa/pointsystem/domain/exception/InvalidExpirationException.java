package com.musinsa.pointsystem.domain.exception;

public class InvalidExpirationException extends PointException {

    private static final String USER_MESSAGE_BELOW_MIN = "최소 만료일보다 작습니다.";
    private static final String USER_MESSAGE_ABOVE_MAX = "최대 만료일을 초과했습니다.";

    private InvalidExpirationException(String userMessage, String internalMessage) {
        super(userMessage, internalMessage);
    }

    public static InvalidExpirationException belowMinimum(int days, int minDays) {
        return new InvalidExpirationException(
                USER_MESSAGE_BELOW_MIN,
                String.format("최소 만료일 미달. 요청: %d일, 최소: %d일", days, minDays));
    }

    public static InvalidExpirationException aboveMaximum(int days, int maxDays) {
        return new InvalidExpirationException(
                USER_MESSAGE_ABOVE_MAX,
                String.format("최대 만료일 초과. 요청: %d일, 최대: %d일", days, maxDays));
    }
}
