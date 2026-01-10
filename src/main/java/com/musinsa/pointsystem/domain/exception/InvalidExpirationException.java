package com.musinsa.pointsystem.domain.exception;

public class InvalidExpirationException extends PointException {

    public InvalidExpirationException(String message) {
        super(message);
    }

    public static InvalidExpirationException belowMinimum(int days, int minDays) {
        return new InvalidExpirationException(
                "최소 만료일보다 작습니다. 요청: " + days + "일, 최소: " + minDays + "일");
    }

    public static InvalidExpirationException aboveMaximum(int days, int maxDays) {
        return new InvalidExpirationException(
                "최대 만료일을 초과했습니다. 요청: " + days + "일, 최대: " + maxDays + "일");
    }
}
