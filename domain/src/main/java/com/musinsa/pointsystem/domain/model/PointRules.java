package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 포인트 비즈니스 규칙 (Enterprise Business Rules)
 * - 순수 함수로 구성
 * - 상태를 갖지 않음
 */
public final class PointRules {

    private PointRules() {}

    // =====================================================
    // 잔액 관련 규칙
    // =====================================================

    /**
     * 사용 가능한 총 잔액 계산
     */
    public static long calculateAvailableBalance(List<PointLedger> ledgers, LocalDateTime now) {
        return ledgers.stream()
                .filter(l -> isAvailable(l, now))
                .mapToLong(PointLedger::availableAmount)
                .sum();
    }

    /**
     * Ledger가 사용 가능한지 확인
     */
    public static boolean isAvailable(PointLedger ledger, LocalDateTime now) {
        return !ledger.canceled()
                && !isExpired(ledger, now)
                && ledger.availableAmount() > 0;
    }

    /**
     * Ledger가 만료되었는지 확인
     */
    public static boolean isExpired(PointLedger ledger, LocalDateTime now) {
        return ledger.expiredAt().isBefore(now);
    }

    // =====================================================
    // 적립 검증 규칙
    // =====================================================

    /**
     * 적립 금액 검증
     */
    public static void validateEarnAmount(long amount, EarnPolicyConfig policy) {
        if (amount < policy.minAmount().getValue()) {
            throw InvalidEarnAmountException.belowMinimum(amount, policy.minAmount().getValue());
        }
        if (amount > policy.maxAmount().getValue()) {
            throw InvalidEarnAmountException.aboveMaximum(amount, policy.maxAmount().getValue());
        }
    }

    /**
     * 만료일 검증
     */
    public static void validateExpirationDays(Integer expirationDays, EarnPolicyConfig policy) {
        if (expirationDays == null) return;

        if (expirationDays < policy.minExpirationDays()) {
            throw InvalidExpirationException.belowMinimum(expirationDays, policy.minExpirationDays());
        }
        if (expirationDays > policy.maxExpirationDays()) {
            throw InvalidExpirationException.aboveMaximum(expirationDays, policy.maxExpirationDays());
        }
    }

    /**
     * 최대 잔액 초과 검증
     */
    public static void validateMaxBalance(long currentBalance, long earnAmount, EarnPolicyConfig policy) {
        if (currentBalance + earnAmount > policy.maxBalance().getValue()) {
            throw new MaxBalanceExceededException(currentBalance, earnAmount, policy.maxBalance().getValue());
        }
    }

    /**
     * 적립 전체 검증
     */
    public static void validateEarn(long amount, long currentBalance, Integer expirationDays, EarnPolicyConfig policy) {
        validateEarnAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(currentBalance, amount, policy);
    }

    // =====================================================
    // 적립 취소 검증 규칙
    // =====================================================

    /**
     * 적립 취소 가능 여부 확인
     */
    public static void validateCancelEarn(PointLedger ledger) {
        if (ledger.canceled()) {
            throw new PointLedgerAlreadyCanceledException(ledger.id());
        }
        if (ledger.earnedAmount() != ledger.availableAmount()) {
            throw new PointLedgerAlreadyUsedException(ledger.id());
        }
    }

    // =====================================================
    // 사용 관련 규칙
    // =====================================================

    /**
     * 잔액 충분 여부 검증
     */
    public static void validateSufficientBalance(long availableBalance, long useAmount) {
        if (availableBalance < useAmount) {
            throw new InsufficientPointException(useAmount, availableBalance);
        }
    }

    /**
     * 사용 가능한 Ledger 목록 (우선순위 정렬)
     * - 수기 적립 우선, 만료일 빠른 순
     */
    public static List<PointLedger> getAvailableLedgersSorted(List<PointLedger> ledgers, LocalDateTime now) {
        return ledgers.stream()
                .filter(l -> isAvailable(l, now))
                .sorted(Comparator
                        .comparing(PointLedger::isManual).reversed()
                        .thenComparing(PointLedger::expiredAt))
                .toList();
    }

    // =====================================================
    // 사용 취소 관련 규칙
    // =====================================================

    /**
     * 특정 주문에서 취소 가능한 금액 계산
     */
    public static long calculateCancelableAmount(List<LedgerEntry> entries, String orderId) {
        long netAmount = entries.stream()
                .filter(e -> orderId != null && orderId.equals(e.orderId()))
                .filter(e -> e.type() == EntryType.USE || e.type() == EntryType.USE_CANCEL)
                .mapToLong(LedgerEntry::amount)
                .sum();

        return Math.abs(Math.min(netAmount, 0));
    }

    /**
     * 취소 가능 금액 검증
     */
    public static void validateCancelAmount(long cancelAmount, long cancelableAmount) {
        if (cancelableAmount == 0) {
            throw new InvalidCancelAmountException(cancelAmount, 0L);
        }
        if (cancelAmount > cancelableAmount) {
            throw new InvalidCancelAmountException(cancelAmount, cancelableAmount);
        }
    }

    /**
     * 특정 Ledger의 주문별 취소 가능 금액 계산
     * - USE Entry의 합계 (음수) + USE_CANCEL Entry의 합계 (양수)
     * - 결과가 음수면 아직 취소 가능한 금액이 있음
     */
    public static long calculateCancelableAmountForLedger(List<LedgerEntry> entries, String orderId) {
        long netAmount = entries.stream()
                .filter(e -> orderId != null && orderId.equals(e.orderId()))
                .filter(e -> e.type() == EntryType.USE || e.type() == EntryType.USE_CANCEL)
                .mapToLong(LedgerEntry::amount)
                .sum();

        // 음수면 사용 중인 금액이므로 절대값 반환
        return Math.abs(Math.min(netAmount, 0));
    }

    /**
     * 신규 Ledger의 만료일 계산
     * - 사용취소 시 만료된 Ledger를 복원할 때 사용
     */
    public static LocalDateTime calculateNewExpirationDate(LocalDateTime now, int defaultExpirationDays) {
        return now.plusDays(defaultExpirationDays);
    }
}
