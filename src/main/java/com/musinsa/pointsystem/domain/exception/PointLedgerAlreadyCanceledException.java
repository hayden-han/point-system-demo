package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerAlreadyCanceledException extends PointException {

    public PointLedgerAlreadyCanceledException(UUID ledgerId) {
        super("이미 취소된 적립건입니다. ledgerId=" + ledgerId);
    }
}
