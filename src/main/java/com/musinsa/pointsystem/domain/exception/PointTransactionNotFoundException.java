package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointTransactionNotFoundException extends PointException {

    public PointTransactionNotFoundException(UUID transactionId) {
        super("트랜잭션을 찾을 수 없습니다. transactionId=" + transactionId);
    }
}
