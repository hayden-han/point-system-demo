package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerAlreadyCanceledException extends PointException {

    private static final String USER_MESSAGE = "이미 취소된 적립건입니다.";

    public PointLedgerAlreadyCanceledException(UUID ledgerId) {
        super(USER_MESSAGE,
              String.format("이미 취소된 적립건. ledgerId=%s", ledgerId));
    }
}
