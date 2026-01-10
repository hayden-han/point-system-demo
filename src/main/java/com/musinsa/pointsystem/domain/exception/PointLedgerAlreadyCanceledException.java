package com.musinsa.pointsystem.domain.exception;

public class PointLedgerAlreadyCanceledException extends PointException {

    public PointLedgerAlreadyCanceledException(Long ledgerId) {
        super("이미 취소된 적립건입니다. ledgerId=" + ledgerId);
    }
}
