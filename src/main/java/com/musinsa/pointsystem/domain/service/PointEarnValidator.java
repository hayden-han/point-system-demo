package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;

public class PointEarnValidator {

    /**
     * 적립 요청에 대한 전체 유효성 검증
     */
    public void validate(Long amount, Integer expirationDays, MemberPoint memberPoint, EarnPolicyConfig policy) {
        validateAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(memberPoint, amount, policy);
    }

    public void validateAmount(Long amount, EarnPolicyConfig policy) {
        if (amount < policy.getMinAmount()) {
            throw InvalidEarnAmountException.belowMinimum(amount, policy.getMinAmount());
        }
        if (amount > policy.getMaxAmount()) {
            throw InvalidEarnAmountException.aboveMaximum(amount, policy.getMaxAmount());
        }
    }

    public void validateMaxBalance(MemberPoint memberPoint, Long earnAmount, EarnPolicyConfig policy) {
        if (!memberPoint.canEarn(earnAmount, policy.getMaxBalance())) {
            throw new MaxBalanceExceededException(
                    memberPoint.getTotalBalance(), earnAmount, policy.getMaxBalance());
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
