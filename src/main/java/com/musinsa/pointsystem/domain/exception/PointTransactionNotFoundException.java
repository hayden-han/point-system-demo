package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointTransactionNotFoundException extends PointException {

    private static final String USER_MESSAGE = "거래 내역을 찾을 수 없습니다.";

    public PointTransactionNotFoundException(UUID transactionId) {
        super(USER_MESSAGE,
              String.format("트랜잭션 없음. transactionId=%s", transactionId));
    }
}
