package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;

public class PointEarnValidator {

    /**
     * 적립 요청에 대한 전체 유효성 검증
     */
    public void validate(PointAmount amount, Integer expirationDays, MemberPoint memberPoint, EarnPolicyConfig policy) {
        validateAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(memberPoint, amount, policy);
    }

    public void validateAmount(PointAmount amount, EarnPolicyConfig policy) {
        if (amount.isLessThan(policy.getMinAmount())) {
            throw InvalidEarnAmountException.belowMinimum(amount.getValue(), policy.getMinAmount().getValue());
        }
        if (amount.isGreaterThan(policy.getMaxAmount())) {
            throw InvalidEarnAmountException.aboveMaximum(amount.getValue(), policy.getMaxAmount().getValue());
        }
    }

    public void validateMaxBalance(MemberPoint memberPoint, PointAmount earnAmount, EarnPolicyConfig policy) {
        if (!memberPoint.canEarn(earnAmount, policy.getMaxBalance())) {
            throw new MaxBalanceExceededException(
                    memberPoint.getTotalBalance().getValue(), earnAmount.getValue(), policy.getMaxBalance().getValue());
        }
    }

    public void validateExpirationDays(Integer expirationDays, EarnPolicyConfig policy) {
        if (expirationDays != null) {
            if (expirationDays < policy.getMinExpirationDays()) {
                throw InvalidExpirationException.belowMinimum(expirationDays, policy.getMinExpirationDays());
            }
            if (expirationDays > policy.getMaxExpirationDays()) {
                throw InvalidExpirationException.aboveMaximum(expirationDays, policy.getMaxExpirationDays());
            }
        }
    }
}
