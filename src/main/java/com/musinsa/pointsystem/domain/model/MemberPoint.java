package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * 회원 포인트 Aggregate Root
 * - 불변 record 패턴
 * - 모든 상태 변경 시 새 객체 반환
 */
public record MemberPoint(
        UUID memberId,
        PointAmount totalBalance,
        List<PointLedger> ledgers
) {
    // Compact constructor - 기본값 및 불변 리스트
    public MemberPoint {
        if (totalBalance == null) {
            totalBalance = PointAmount.ZERO;
        }
        // 불변 리스트로 복사
        ledgers = ledgers != null ? List.copyOf(ledgers) : List.of();
    }

    // 기존 코드 호환성을 위한 getter 메서드들
    public UUID getMemberId() {
        return memberId;
    }

    public PointAmount getTotalBalance() {
        return totalBalance;
    }

    public List<PointLedger> getLedgers() {
        return ledgers;
    }

    // Static Factory Method
    public static MemberPoint create(UUID memberId) {
        return new MemberPoint(memberId, PointAmount.ZERO, List.of());
    }

    // =====================================================
    // 적립 (Earn)
    // =====================================================

    /**
     * 적립 결과
     */
    public record EarnResult(MemberPoint memberPoint, PointLedger ledger) {}

    /**
     * 포인트 적립 (불변 - 새 객체 반환)
     */
    public EarnResult earn(PointAmount amount, EarnType earnType,
                           LocalDateTime expiredAt, EarnPolicyConfig policy) {
        validateEarnAmount(amount, policy);
        validateMaxBalance(amount, policy);

        PointLedger ledger = PointLedger.create(memberId, amount, earnType, expiredAt);
        List<PointLedger> newLedgers = new ArrayList<>(ledgers);
        newLedgers.add(ledger);

        MemberPoint updated = new MemberPoint(
                memberId,
                totalBalance.add(amount),
                newLedgers
        );
        return new EarnResult(updated, ledger);
    }

    /**
     * 만료일 검증 포함 적립 (불변 - 새 객체 반환)
     */
    public EarnResult earnWithExpirationValidation(PointAmount amount, EarnType earnType,
                                                    Integer expirationDays, EarnPolicyConfig policy) {
        validateEarnAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(amount, policy);

        LocalDateTime expiredAt = policy.calculateExpirationDate(expirationDays);
        PointLedger ledger = PointLedger.create(memberId, amount, earnType, expiredAt);
        List<PointLedger> newLedgers = new ArrayList<>(ledgers);
        newLedgers.add(ledger);

        MemberPoint updated = new MemberPoint(
                memberId,
                totalBalance.add(amount),
                newLedgers
        );
        return new EarnResult(updated, ledger);
    }

    // =====================================================
    // 적립취소 (Cancel Earn)
    // =====================================================

    /**
     * 적립취소 결과
     */
    public record CancelEarnResult(MemberPoint memberPoint, PointAmount canceledAmount) {}

    /**
     * 적립 취소 (불변 - 새 객체 반환)
     */
    public CancelEarnResult cancelEarn(UUID ledgerId) {
        PointLedger targetLedger = findLedgerById(ledgerId);
        PointAmount canceledAmount = targetLedger.earnedAmount();
        PointLedger canceledLedger = targetLedger.cancel();

        List<PointLedger> newLedgers = ledgers.stream()
                .map(l -> l.id().equals(ledgerId) ? canceledLedger : l)
                .toList();

        MemberPoint updated = new MemberPoint(
                memberId,
                totalBalance.subtract(canceledAmount),
                newLedgers
        );
        return new CancelEarnResult(updated, canceledAmount);
    }

    // =====================================================
    // 사용 (Use)
    // =====================================================

    /**
     * 사용 상세
     */
    public record UsageDetail(UUID ledgerId, PointAmount usedAmount) {}

    /**
     * 사용 결과
     */
    public record UsageResult(MemberPoint memberPoint, List<UsageDetail> usageDetails) {}

    /**
     * 포인트 사용 (불변 - 새 객체 반환)
     */
    public UsageResult use(PointAmount amount) {
        if (totalBalance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), totalBalance.getValue());
        }

        List<PointLedger> availableLedgers = getAvailableLedgersSorted();
        DeductResult deductResult = deductFromLedgers(availableLedgers, amount);

        // 사용된 적립건들로 ledgers 업데이트
        Map<UUID, PointLedger> updatedLedgersMap = deductResult.updatedLedgers().stream()
                .collect(Collectors.toMap(PointLedger::id, l -> l));

        List<PointLedger> newLedgers = ledgers.stream()
                .map(l -> updatedLedgersMap.getOrDefault(l.id(), l))
                .toList();

        MemberPoint updated = new MemberPoint(
                memberId,
                totalBalance.subtract(amount),
                newLedgers
        );
        return new UsageResult(updated, deductResult.usageDetails());
    }

    private record DeductResult(List<PointLedger> updatedLedgers, List<UsageDetail> usageDetails) {}

    private DeductResult deductFromLedgers(List<PointLedger> availableLedgers, PointAmount amount) {
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

        return new DeductResult(updatedLedgers, usageDetails);
    }

    // =====================================================
    // 사용취소 (Cancel Use)
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
     * 복구 결과
     */
    public record RestoreResult(
            MemberPoint memberPoint,
            List<PointLedger> restoredLedgers,
            List<PointLedger> newLedgers,
            List<PointUsageDetail> updatedUsageDetails
    ) {}

    /**
     * 사용 취소 (불변 - 새 객체 반환)
     */
    public RestoreResult cancelUse(List<PointUsageDetail> usageDetails,
                                   PointAmount cancelAmount,
                                   int defaultExpirationDays,
                                   UUID cancelTransactionId) {
        // 취소 가능 금액 검증
        PointAmount totalCancelable = usageDetails.stream()
                .map(PointUsageDetail::getCancelableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);

        if (cancelAmount.isGreaterThan(totalCancelable)) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), totalCancelable.getValue());
        }

        // 적립건 ID → 적립건 매핑
        Map<UUID, PointLedger> ledgerMap = ledgers.stream()
                .collect(Collectors.toMap(PointLedger::id, l -> l));

        List<PointLedger> restoredLedgers = new ArrayList<>();
        List<PointLedger> newLedgers = new ArrayList<>();
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

            if (originalLedger != null && originalLedger.isExpired()) {
                // 만료된 적립건 → 신규 적립건 생성
                PointLedger newLedger = PointLedger.createFromCancelUse(
                        memberId,
                        cancelResult.canceledAmount(),
                        originalLedger.earnType(),
                        LocalDateTime.now().plusDays(defaultExpirationDays),
                        cancelTransactionId
                );
                newLedgers.add(newLedger);
            } else if (originalLedger != null) {
                // 만료되지 않은 적립건 → 복구
                PointLedger restored = originalLedger.restore(cancelResult.canceledAmount());
                restoredLedgers.add(restored);
                ledgerUpdates.put(restored.id(), restored);
            }
        }

        // ledgers 업데이트 (복구된 것 + 신규 생성)
        List<PointLedger> finalLedgers = new ArrayList<>();
        for (PointLedger ledger : ledgers) {
            finalLedgers.add(ledgerUpdates.getOrDefault(ledger.id(), ledger));
        }
        finalLedgers.addAll(newLedgers);

        MemberPoint updated = new MemberPoint(
                memberId,
                totalBalance.add(cancelAmount),
                finalLedgers
        );

        return new RestoreResult(updated, restoredLedgers, newLedgers, updatedUsageDetails);
    }

    // =====================================================
    // 헬퍼 메서드
    // =====================================================

    private List<PointLedger> getAvailableLedgersSorted() {
        return ledgers.stream()
                .filter(PointLedger::isAvailable)
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

    public boolean canEarn(PointAmount amount, PointAmount maxBalance) {
        return totalBalance.add(amount).isLessThanOrEqual(maxBalance);
    }

    public boolean hasEnoughBalance(PointAmount amount) {
        return totalBalance.isGreaterThanOrEqual(amount);
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

    private void validateMaxBalance(PointAmount earnAmount, EarnPolicyConfig policy) {
        if (!canEarn(earnAmount, policy.maxBalance())) {
            throw new MaxBalanceExceededException(
                    totalBalance.getValue(), earnAmount.getValue(), policy.maxBalance().getValue());
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
