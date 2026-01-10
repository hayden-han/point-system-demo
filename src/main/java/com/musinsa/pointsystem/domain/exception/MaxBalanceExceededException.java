package com.musinsa.pointsystem.domain.exception;

public class MaxBalanceExceededException extends PointException {

    public MaxBalanceExceededException(Long currentBalance, Long earnAmount, Long maxBalance) {
        super("최대 보유 가능 금액을 초과합니다. 현재: " + currentBalance +
              ", 적립: " + earnAmount + ", 최대: " + maxBalance);
    }
}
