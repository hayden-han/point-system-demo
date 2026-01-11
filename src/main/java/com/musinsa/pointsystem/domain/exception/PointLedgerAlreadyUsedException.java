package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerAlreadyUsedException extends PointException {

    public PointLedgerAlreadyUsedException(UUID ledgerId) {
        super("이미 사용된 적립건은 취소할 수 없습니다. ledgerId=" + ledgerId);
    }
}
