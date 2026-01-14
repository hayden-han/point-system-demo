package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * 회원 포인트 Aggregate Root
 * - 불변 record 패턴
 * - 모든 상태 변경 시 새 객체 반환
 * - totalBalance는 조회 시점에 계산 (v2)
 */
public record MemberPoint(
        UUID memberId,
        List<PointLedger> ledgers
) {
    public MemberPoint {
        ledgers = ledgers != null ? List.copyOf(ledgers) : List.of();
    }

    public static MemberPoint create(UUID memberId) {
        return new MemberPoint(memberId, List.of());
    }

    public static MemberPoint of(UUID memberId, List<PointLedger> ledgers) {
        return new MemberPoint(memberId, ledgers);
    }

    // =====================================================
    // 잔액 조회 (v2: 조회 시점 계산)
    // =====================================================

    /**
     * 총 잔액 - 조회 시점에 계산
     * - 만료되지 않고, 취소되지 않은 Ledger의 availableAmount 합계
     */
    public PointAmount getTotalBalance(LocalDateTime now) {
        return ledgers.stream()
                .filter(l -> l.isAvailable(now))
                .map(PointLedger::availableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);
    }

    // =====================================================
    // 적립 (Earn)
    // =====================================================

    public record EarnResult(MemberPoint memberPoint, PointLedger ledger) {}

    /**
     * 적립 검증 (금액, 최대 잔액)
     */
    public void validateEarn(PointAmount amount, EarnPolicyConfig policy, LocalDateTime now) {
        validateEarnAmount(amount, policy);
        validateMaxBalance(amount, policy, now);
    }

    /**
     * 적립 검증 (금액, 만료일, 최대 잔액)
     */
    public void validateEarnWithExpiration(PointAmount amount, Integer expirationDays, EarnPolicyConfig policy, LocalDateTime now) {
        validateEarnAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(amount, policy, now);
    }

    /**
     * 적립 검증 - 외부에서 조회한 잔액으로 검증 (성능 최적화용)
     * <p>
     * Ledger 로드 없이 DB에서 직접 조회한 잔액으로 검증할 때 사용.
     * 적립 시에는 기존 Ledger를 수정하지 않으므로 이 방식이 효율적.
     *
     * @param amount 적립 금액
     * @param currentBalance 현재 잔액 (외부에서 조회)
     * @param expirationDays 만료일 (null이면 기본값 사용)
     * @param policy 적립 정책
     */
    public static void validateEarnWithBalance(PointAmount amount, PointAmount currentBalance,
                                                Integer expirationDays, EarnPolicyConfig policy) {
        // 금액 검증
        if (amount.isLessThan(policy.minAmount())) {
            throw InvalidEarnAmountException.belowMinimum(amount.getValue(), policy.minAmount().getValue());
        }
        if (amount.isGreaterThan(policy.maxAmount())) {
            throw InvalidEarnAmountException.aboveMaximum(amount.getValue(), policy.maxAmount().getValue());
        }

        // 만료일 검증
        if (expirationDays != null) {
            if (expirationDays < policy.minExpirationDays()) {
                throw InvalidExpirationException.belowMinimum(expirationDays, policy.minExpirationDays());
            }
            if (expirationDays > policy.maxExpirationDays()) {
                throw InvalidExpirationException.aboveMaximum(expirationDays, policy.maxExpirationDays());
            }
        }

        // 최대 잔액 검증
        if (currentBalance.add(amount).isGreaterThan(policy.maxBalance())) {
            throw new MaxBalanceExceededException(
                    currentBalance.getValue(), amount.getValue(), policy.maxBalance().getValue());
        }
    }

    /**
     * Ledger 추가 (불변 - 새 객체 반환)
     */
    public MemberPoint addLedger(PointLedger ledger) {
        List<PointLedger> newLedgers = new ArrayList<>(ledgers);
        newLedgers.add(ledger);
        return new MemberPoint(memberId, newLedgers);
    }

    // =====================================================
    // 적립취소 (Cancel Earn)
    // =====================================================

    public record CancelEarnResult(MemberPoint memberPoint, PointAmount canceledAmount) {}

    /**
     * 적립 취소 (v2: IdGenerator, LocalDateTime 사용)
     */
    public CancelEarnResult cancelEarn(UUID ledgerId, IdGenerator idGenerator, LocalDateTime now) {
        PointLedger targetLedger = findLedgerById(ledgerId);
        PointLedger.CancelResult cancelResult = targetLedger.cancel(idGenerator, now);

        List<PointLedger> newLedgers = ledgers.stream()
                .map(l -> l.id().equals(ledgerId) ? cancelResult.ledger() : l)
                .toList();

        MemberPoint updated = new MemberPoint(memberId, newLedgers);
        return new CancelEarnResult(updated, cancelResult.canceledAmount());
    }

    // =====================================================
    // 사용 (Use) - v2
    // =====================================================

    public record UsageDetail(UUID ledgerId, PointAmount usedAmount) {}

    public record UsageResult(MemberPoint memberPoint, List<UsageDetail> usageDetails) {}

    /**
     * 포인트 사용 (v2: orderId, IdGenerator, now 추가)
     */
    public UsageResult use(PointAmount amount, String orderId, IdGenerator idGenerator, LocalDateTime now) {
        PointAmount balance = getTotalBalance(now);
        if (balance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), balance.getValue());
        }

        List<PointLedger> availableLedgers = getAvailableLedgersSorted(now);
        DeductResult deductResult = deductFromLedgers(availableLedgers, amount, orderId, idGenerator, now);

        Map<UUID, PointLedger> updatedLedgersMap = deductResult.updatedLedgers().stream()
                .collect(Collectors.toMap(PointLedger::id, l -> l));

        List<PointLedger> newLedgers = ledgers.stream()
                .map(l -> updatedLedgersMap.getOrDefault(l.id(), l))
                .toList();

        MemberPoint updated = new MemberPoint(memberId, newLedgers);
        return new UsageResult(updated, deductResult.usageDetails());
    }

    private record DeductResult(List<PointLedger> updatedLedgers, List<UsageDetail> usageDetails) {}

    private DeductResult deductFromLedgers(List<PointLedger> availableLedgers, PointAmount amount,
                                           String orderId, IdGenerator idGenerator, LocalDateTime now) {
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<UsageDetail> usageDetails = new ArrayList<>();
        PointAmount remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount.isZero()) {
                break;
            }

            PointLedger.UseResult useResult = ledger.use(remainingAmount, orderId, idGenerator, now);
            remainingAmount = remainingAmount.subtract(useResult.usedAmount());
            updatedLedgers.add(useResult.ledger());
            usageDetails.add(new UsageDetail(ledger.id(), useResult.usedAmount()));
        }

        return new DeductResult(updatedLedgers, usageDetails);
    }

    // =====================================================
    // 사용취소 (Cancel Use) - v2
    // =====================================================

    public record CancelUseResult(
            MemberPoint memberPoint,
            List<PointLedger> restoredLedgers,
            List<PointLedger> newLedgers
    ) {}

    /**
     * 사용 취소 (v2: LedgerEntry 기반)
     * - 해당 주문의 Entry를 찾아 취소 Entry 추가
     * - 만료된 Ledger는 신규 Ledger 생성
     */
    public CancelUseResult cancelUse(
            String orderId,
            PointAmount cancelAmount,
            LocalDateTime now,
            IdGenerator idGenerator,
            int defaultExpirationDays
    ) {
        // 해당 주문에서 사용된 Ledger 찾기
        List<PointLedger> ledgersWithOrder = ledgers.stream()
                .filter(l -> l.getCancelableAmountByOrder(orderId).isPositive())
                .toList();

        if (ledgersWithOrder.isEmpty()) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), 0L);
        }

        // 취소 가능 금액 검증
        PointAmount totalCancelable = ledgersWithOrder.stream()
                .map(l -> l.getCancelableAmountByOrder(orderId))
                .reduce(PointAmount.ZERO, PointAmount::add);

        if (cancelAmount.isGreaterThan(totalCancelable)) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), totalCancelable.getValue());
        }

        List<PointLedger> restoredLedgers = new ArrayList<>();
        List<PointLedger> newLedgers = new ArrayList<>();
        Map<UUID, PointLedger> ledgerUpdates = new HashMap<>();

        PointAmount remainingCancelAmount = cancelAmount;

        for (PointLedger ledger : ledgersWithOrder) {
            if (remainingCancelAmount.isZero()) {
                break;
            }

            PointLedger.CancelUseResult result = ledger.cancelUse(
                    orderId, remainingCancelAmount, now, idGenerator, defaultExpirationDays
            );

            remainingCancelAmount = remainingCancelAmount.subtract(result.canceledAmount());
            ledgerUpdates.put(ledger.id(), result.ledger());

            if (!ledger.isExpired(now)) {
                restoredLedgers.add(result.ledger());
            }

            if (result.newLedger() != null) {
                newLedgers.add(result.newLedger());
            }
        }

        // ledgers 업데이트 (기존 + 신규)
        List<PointLedger> finalLedgers = new ArrayList<>();
        for (PointLedger ledger : ledgers) {
            finalLedgers.add(ledgerUpdates.getOrDefault(ledger.id(), ledger));
        }
        finalLedgers.addAll(newLedgers);

        MemberPoint updated = new MemberPoint(memberId, finalLedgers);
        return new CancelUseResult(updated, restoredLedgers, newLedgers);
    }

    // =====================================================
    // 헬퍼 메서드
    // =====================================================

    private List<PointLedger> getAvailableLedgersSorted(LocalDateTime now) {
        return ledgers.stream()
                .filter(l -> l.isAvailable(now))
                .sorted(comparing(PointLedger::isManual).reversed()
                        .thenComparing(PointLedger::expiredAt))
                .toList();
    }

    public PointLedger findLedgerById(UUID ledgerId) {
        return ledgers.stream()
                .filter(l -> l.id().equals(ledgerId))
                .findFirst()
                .orElseThrow(() -> new PointLedgerNotFoundException(ledgerId));
    }

    public boolean canEarn(PointAmount amount, PointAmount maxBalance, LocalDateTime now) {
        return getTotalBalance(now).add(amount).isLessThanOrEqual(maxBalance);
    }

    public boolean hasEnoughBalance(PointAmount amount, LocalDateTime now) {
        return getTotalBalance(now).isGreaterThanOrEqual(amount);
    }

    // =====================================================
    // 검증 메서드
    // =====================================================

    private void validateEarnAmount(PointAmount amount, EarnPolicyConfig policy) {
        if (amount.isLessThan(policy.minAmount())) {
            throw InvalidEarnAmountException.belowMinimum(amount.getValue(), policy.minAmount().getValue());
        }
        if (amount.isGreaterThan(policy.maxAmount())) {
            throw InvalidEarnAmountException.aboveMaximum(amount.getValue(), policy.maxAmount().getValue());
        }
    }

    private void validateMaxBalance(PointAmount earnAmount, EarnPolicyConfig policy, LocalDateTime now) {
        if (!canEarn(earnAmount, policy.maxBalance(), now)) {
            throw new MaxBalanceExceededException(
                    getTotalBalance(now).getValue(), earnAmount.getValue(), policy.maxBalance().getValue());
        }
    }

    private void validateExpirationDays(Integer expirationDays, EarnPolicyConfig policy) {
        if (expirationDays != null) {
            if (expirationDays < policy.minExpirationDays()) {
                throw InvalidExpirationException.belowMinimum(expirationDays, policy.minExpirationDays());
            }
            if (expirationDays > policy.maxExpirationDays()) {
                throw InvalidExpirationException.aboveMaximum(expirationDays, policy.maxExpirationDays());
            }
        }
    }
}
