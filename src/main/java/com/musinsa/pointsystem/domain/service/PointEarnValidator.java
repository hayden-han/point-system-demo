package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import org.springframework.stereotype.Component;

@Component
public class PointEarnValidator {

    public void validateAmount(Long amount, Long minAmount, Long maxAmount) {
        if (amount < minAmount) {
            throw InvalidEarnAmountException.belowMinimum(amount, minAmount);
        }
        if (amount > maxAmount) {
            throw InvalidEarnAmountException.aboveMaximum(amount, maxAmount);
        }
    }

    public void validateMaxBalance(MemberPoint memberPoint, Long earnAmount, Long maxBalance) {
        if (!memberPoint.canEarn(earnAmount, maxBalance)) {
            throw new MaxBalanceExceededException(
                    memberPoint.getTotalBalance(), earnAmount, maxBalance);
        }
    }

    public void validateExpirationDays(Integer expirationDays, Long minDays, Long maxDays) {
        if (expirationDays != null) {
            if (expirationDays < minDays) {
                throw InvalidExpirationException.belowMinimum(expirationDays, minDays.intValue());
            }
            if (expirationDays > maxDays) {
                throw InvalidExpirationException.aboveMaximum(expirationDays, maxDays.intValue());
            }
        }
    }
}
