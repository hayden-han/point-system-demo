package com.musinsa.pointsystem.domain.exception;

public class PointTransactionNotFoundException extends PointException {

    public PointTransactionNotFoundException(Long transactionId) {
        super("트랜잭션을 찾을 수 없습니다. transactionId=" + transactionId);
    }
}
