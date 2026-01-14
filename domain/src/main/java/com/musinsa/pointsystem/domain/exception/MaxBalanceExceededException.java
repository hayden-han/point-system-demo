package com.musinsa.pointsystem.domain.exception;

public class MaxBalanceExceededException extends PointException {

    private static final String USER_MESSAGE = "최대 보유 가능 금액을 초과합니다.";

    public MaxBalanceExceededException(Long currentBalance, Long earnAmount, Long maxBalance) {
        super(USER_MESSAGE,
              String.format("최대 보유 금액 초과. 현재: %d, 적립: %d, 최대: %d",
                           currentBalance, earnAmount, maxBalance));
    }
}
