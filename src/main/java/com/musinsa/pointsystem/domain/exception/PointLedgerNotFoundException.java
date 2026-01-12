package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerNotFoundException extends PointException {

    private static final String USER_MESSAGE = "적립건을 찾을 수 없습니다.";

    public PointLedgerNotFoundException(UUID ledgerId) {
        super(USER_MESSAGE,
              String.format("적립건 없음. ledgerId=%s", ledgerId));
    }
}
