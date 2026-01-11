package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class PointLedgerNotFoundException extends PointException {

    public PointLedgerNotFoundException(UUID ledgerId) {
        super("적립건을 찾을 수 없습니다. ledgerId=" + ledgerId);
    }
}
