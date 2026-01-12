package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerAlreadyUsedException extends PointException {

    private static final String USER_MESSAGE = "이미 사용된 적립건은 취소할 수 없습니다.";

    public PointLedgerAlreadyUsedException(UUID ledgerId) {
        super(USER_MESSAGE,
              String.format("이미 사용된 적립건. ledgerId=%s", ledgerId));
    }
}
