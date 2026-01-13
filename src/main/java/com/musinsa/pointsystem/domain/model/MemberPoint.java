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

    /**
     * @deprecated getTotalBalance(LocalDateTime) 사용 권장
     */
    @Deprecated
    public PointAmount totalBalance() {
        return getTotalBalance(LocalDateTime.now(java.time.ZoneOffset.UTC));
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
     * @deprecated validateEarn(PointAmount, EarnPolicyConfig, LocalDateTime) 사용 권장
     */
    @Deprecated
    public void validateEarn(PointAmount amount, EarnPolicyConfig policy) {
        validateEarn(amount, policy, LocalDateTime.now(java.time.ZoneOffset.UTC));
    }

    /**
     * @deprecated validateEarnWithExpiration(PointAmount, Integer, EarnPolicyConfig, LocalDateTime) 사용 권장
     */
    @Deprecated
    public void validateEarnWithExpiration(PointAmount amount, Integer expirationDays, EarnPolicyConfig policy) {
        validateEarnWithExpiration(amount, expirationDays, policy, LocalDateTime.now(java.time.ZoneOffset.UTC));
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

    /**
     * @deprecated cancelEarn(UUID, IdGenerator, LocalDateTime) 사용 권장
     */
    @Deprecated
    public CancelEarnResult cancelEarn(UUID ledgerId) {
        PointLedger targetLedger = findLedgerById(ledgerId);
        PointAmount canceledAmount = targetLedger.earnedAmount();
        PointLedger canceledLedger = targetLedger.cancel();

        List<PointLedger> newLedgers = ledgers.stream()
                .map(l -> l.id().equals(ledgerId) ? canceledLedger : l)
                .toList();

        MemberPoint updated = new MemberPoint(memberId, newLedgers);
        return new CancelEarnResult(updated, canceledAmount);
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

    /**
     * @deprecated use(PointAmount, String, IdGenerator, LocalDateTime) 사용 권장
     */
    @Deprecated
    public UsageResult use(PointAmount amount) {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        PointAmount balance = getTotalBalance(now);
        if (balance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), balance.getValue());
        }

        List<PointLedger> availableLedgers = getAvailableLedgersSorted(now);
        DeductResultLegacy deductResult = deductFromLedgersLegacy(availableLedgers, amount);

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

    @Deprecated
    private record DeductResultLegacy(List<PointLedger> updatedLedgers, List<UsageDetail> usageDetails) {}

    @Deprecated
    private DeductResultLegacy deductFromLedgersLegacy(List<PointLedger> availableLedgers, PointAmount amount) {
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<UsageDetail> usageDetails = new ArrayList<>();
        PointAmount remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount.isZero()) {
                break;
            }

            PointLedger.UseResult useResult = ledger.use(remainingAmount);
            remainingAmount = remainingAmount.subtract(useResult.usedAmount());
            updatedLedgers.add(useResult.ledger());
            usageDetails.add(new UsageDetail(ledger.id(), useResult.usedAmount()));
        }

        return new DeductResultLegacy(updatedLedgers, usageDetails);
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
    // 레거시 호환 (PointUsageDetail 기반)
    // =====================================================

    /**
     * 신규 적립건 정보
     */
    public record NewLedgerInfo(
            PointAmount amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID relatedTransactionId
    ) {}

    /**
     * 복구 결과 (레거시)
     */
    public record RestoreResult(
            MemberPoint memberPoint,
            List<PointLedger> restoredLedgers,
            List<PointLedger> newLedgersCreated,
            List<PointUsageDetail> updatedUsageDetails
    ) {}

    /**
     * @deprecated cancelUse(String, PointAmount, LocalDateTime, IdGenerator, int) 사용 권장
     */
    @Deprecated
    public RestoreResult cancelUse(List<PointUsageDetail> usageDetails,
                                   PointAmount cancelAmount,
                                   int defaultExpirationDays,
                                   UUID cancelTransactionId,
                                   com.musinsa.pointsystem.domain.factory.PointFactory pointFactory,
                                   LocalDateTime now) {
        // 취소 가능 금액 검증
        PointAmount totalCancelable = usageDetails.stream()
                .map(PointUsageDetail::getCancelableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);

        if (cancelAmount.isGreaterThan(totalCancelable)) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), totalCancelable.getValue());
        }

        Map<UUID, PointLedger> ledgerMap = ledgers.stream()
                .collect(Collectors.toMap(PointLedger::id, l -> l));

        for (PointUsageDetail detail : usageDetails) {
            if (!ledgerMap.containsKey(detail.ledgerId())) {
                throw new PointLedgerNotFoundException(detail.ledgerId());
            }
        }

        List<PointLedger> restoredLedgers = new ArrayList<>();
        List<PointLedger> newLedgersCreated = new ArrayList<>();
        List<PointUsageDetail> updatedUsageDetails = new ArrayList<>();
        Map<UUID, PointLedger> ledgerUpdates = new HashMap<>();

        PointAmount remainingCancelAmount = cancelAmount;

        for (PointUsageDetail usageDetail : usageDetails) {
            if (remainingCancelAmount.isZero()) {
                break;
            }

            PointUsageDetail.CancelResult cancelResult = usageDetail.cancel(remainingCancelAmount);
            remainingCancelAmount = remainingCancelAmount.subtract(cancelResult.canceledAmount());
            updatedUsageDetails.add(cancelResult.usageDetail());

            PointLedger originalLedger = ledgerMap.get(usageDetail.ledgerId());

            if (originalLedger.isExpired(now)) {
                PointLedger newLedger = pointFactory.createLedgerFromCancelUse(
                        memberId,
                        cancelResult.canceledAmount(),
                        originalLedger.earnType(),
                        now.plusDays(defaultExpirationDays),
                        cancelTransactionId
                );
                newLedgersCreated.add(newLedger);
            } else {
                PointLedger restored = originalLedger.restore(cancelResult.canceledAmount());
                restoredLedgers.add(restored);
                ledgerUpdates.put(restored.id(), restored);
            }
        }

        List<PointLedger> finalLedgers = new ArrayList<>();
        for (PointLedger ledger : ledgers) {
            finalLedgers.add(ledgerUpdates.getOrDefault(ledger.id(), ledger));
        }
        finalLedgers.addAll(newLedgersCreated);

        MemberPoint updated = new MemberPoint(memberId, finalLedgers);
        return new RestoreResult(updated, restoredLedgers, newLedgersCreated, updatedUsageDetails);
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

    @Deprecated
    private List<PointLedger> getAvailableLedgersSorted() {
        return getAvailableLedgersSorted(LocalDateTime.now(java.time.ZoneOffset.UTC));
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

    @Deprecated
    public boolean canEarn(PointAmount amount, PointAmount maxBalance) {
        return canEarn(amount, maxBalance, LocalDateTime.now(java.time.ZoneOffset.UTC));
    }

    public boolean hasEnoughBalance(PointAmount amount, LocalDateTime now) {
        return getTotalBalance(now).isGreaterThanOrEqual(amount);
    }

    @Deprecated
    public boolean hasEnoughBalance(PointAmount amount) {
        return hasEnoughBalance(amount, LocalDateTime.now(java.time.ZoneOffset.UTC));
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
