package com.musinsa.pointsystem.domain.exception;

public class PointLedgerNotFoundException extends PointException {

    public PointLedgerNotFoundException(Long ledgerId) {
        super("적립건을 찾을 수 없습니다. ledgerId=" + ledgerId);
    }
}
