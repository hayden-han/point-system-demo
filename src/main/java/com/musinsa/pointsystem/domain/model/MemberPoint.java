package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.event.*;
import com.musinsa.pointsystem.domain.exception.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * 회원 포인트 Aggregate Root
 * - 불변 record 패턴
 * - 이벤트 소싱 지원 (reconstitute, apply 메서드)
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
    // 이벤트 소싱: Aggregate 복원 (Reconstitute)
    // =====================================================

    /**
     * 이벤트들로부터 Aggregate 상태 복원
     *
     * @param memberId Aggregate ID
     * @param events   이벤트 목록 (버전 순서)
     * @return 복원된 MemberPoint
     */
    public static MemberPoint reconstitute(UUID memberId, List<PointEvent> events) {
        MemberPoint aggregate = MemberPoint.create(memberId);
        for (PointEvent event : events) {
            aggregate = aggregate.apply(event);
        }
        return aggregate;
    }

    /**
     * 스냅샷 + 이벤트로부터 Aggregate 상태 복원
     *
     * @param snapshot 스냅샷
     * @param events   스냅샷 이후 이벤트 목록
     * @return 복원된 MemberPoint
     */
    public static MemberPoint reconstitute(MemberPointSnapshot snapshot, List<PointEvent> events) {
        MemberPoint aggregate = snapshot.toMemberPoint();
        for (PointEvent event : events) {
            aggregate = aggregate.apply(event);
        }
        return aggregate;
    }

    /**
     * 이벤트 적용 (상태 변경)
     */
    public MemberPoint apply(PointEvent event) {
        return switch (event) {
            case PointEarnedEvent e -> applyEarned(e);
            case PointUsedEvent e -> applyUsed(e);
            case PointEarnCanceledEvent e -> applyEarnCanceled(e);
            case PointUseCanceledEvent e -> applyUseCanceled(e);
        };
    }

    private MemberPoint applyEarned(PointEarnedEvent e) {
        PointLedger ledger = new PointLedger(
                e.ledgerId(),
                memberId,
                PointAmount.of(e.amount()),
                PointAmount.of(e.amount()),
                PointAmount.ZERO,
                e.earnType(),
                null,
                e.expiredAt(),
                false,
                e.occurredAt()
        );
        List<PointLedger> newLedgers = new ArrayList<>(ledgers);
        newLedgers.add(ledger);

        return new MemberPoint(
                memberId,
                totalBalance.add(PointAmount.of(e.amount())),
                newLedgers
        );
    }

    private MemberPoint applyUsed(PointUsedEvent e) {
        Map<UUID, Long> usageMap = e.usageDetails().stream()
                .collect(Collectors.toMap(
                        PointUsedEvent.UsageDetail::ledgerId,
                        PointUsedEvent.UsageDetail::usedAmount
                ));

        List<PointLedger> newLedgers = ledgers.stream()
                .map(ledger -> {
                    Long usedAmount = usageMap.get(ledger.id());
                    if (usedAmount != null) {
                        return ledger.use(PointAmount.of(usedAmount)).ledger();
                    }
                    return ledger;
                })
                .toList();

        return new MemberPoint(
                memberId,
                totalBalance.subtract(PointAmount.of(e.amount())),
                newLedgers
        );
    }

    private MemberPoint applyEarnCanceled(PointEarnCanceledEvent e) {
        List<PointLedger> newLedgers = ledgers.stream()
                .map(ledger -> {
                    if (ledger.id().equals(e.ledgerId())) {
                        return ledger.cancel();
                    }
                    return ledger;
                })
                .toList();

        return new MemberPoint(
                memberId,
                totalBalance.subtract(PointAmount.of(e.canceledAmount())),
                newLedgers
        );
    }

    private MemberPoint applyUseCanceled(PointUseCanceledEvent e) {
        // 복구된 적립건 처리
        Map<UUID, Long> restoredMap = e.restoredLedgers().stream()
                .collect(Collectors.toMap(
                        PointUseCanceledEvent.RestoredLedger::ledgerId,
                        PointUseCanceledEvent.RestoredLedger::restoredAmount
                ));

        List<PointLedger> updatedLedgers = new ArrayList<>(ledgers.stream()
                .map(ledger -> {
                    Long restoredAmount = restoredMap.get(ledger.id());
                    if (restoredAmount != null) {
                        return ledger.restore(PointAmount.of(restoredAmount));
                    }
                    return ledger;
                })
                .toList());

        // 신규 적립건 추가
        for (PointUseCanceledEvent.NewLedger newLedger : e.newLedgers()) {
            PointLedger ledger = new PointLedger(
                    newLedger.ledgerId(),
                    memberId,
                    PointAmount.of(newLedger.amount()),
                    PointAmount.of(newLedger.amount()),
                    PointAmount.ZERO,
                    newLedger.earnType(),
                    e.originalTransactionId(),
                    newLedger.expiredAt(),
                    false,
                    e.occurredAt()
            );
            updatedLedgers.add(ledger);
        }

        return new MemberPoint(
                memberId,
                totalBalance.add(PointAmount.of(e.canceledAmount())),
                updatedLedgers
        );
    }

    // =====================================================
    // 이벤트 소싱: 커맨드 처리 → 이벤트 생성 (Process)
    // =====================================================

    /**
     * 적립 이벤트 결과
     */
    public record EarnEventResult(MemberPoint memberPoint, PointEarnedEvent event) {}

    /**
     * 포인트 적립 처리 → 이벤트 생성
     */
    public EarnEventResult processEarn(PointAmount amount, EarnType earnType,
                                        LocalDateTime expiredAt, EarnPolicyConfig policy,
                                        long currentVersion) {
        validateEarnAmount(amount, policy);
        validateMaxBalance(amount, policy);

        PointEarnedEvent event = new PointEarnedEvent(
                UuidGenerator.generate(),
                memberId,
                UuidGenerator.generate(),
                amount.getValue(),
                earnType,
                expiredAt,
                currentVersion + 1,
                LocalDateTime.now()
        );

        MemberPoint updated = apply(event);
        return new EarnEventResult(updated, event);
    }

    /**
     * 사용 이벤트 결과
     */
    public record UseEventResult(MemberPoint memberPoint, PointUsedEvent event) {}

    /**
     * 포인트 사용 처리 → 이벤트 생성
     */
    public UseEventResult processUse(PointAmount amount, String orderId, long currentVersion) {
        if (totalBalance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), totalBalance.getValue());
        }

        List<PointLedger> availableLedgers = getAvailableLedgersSorted();
        List<PointUsedEvent.UsageDetail> usageDetails = new ArrayList<>();
        PointAmount remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount.isZero()) {
                break;
            }
            PointAmount useAmount = remainingAmount.min(ledger.availableAmount());
            usageDetails.add(new PointUsedEvent.UsageDetail(ledger.id(), useAmount.getValue()));
            remainingAmount = remainingAmount.subtract(useAmount);
        }

        UUID transactionId = UuidGenerator.generate();
        PointUsedEvent event = new PointUsedEvent(
                UuidGenerator.generate(),
                memberId,
                transactionId,
                amount.getValue(),
                orderId,
                usageDetails,
                currentVersion + 1,
                LocalDateTime.now()
        );

        MemberPoint updated = apply(event);
        return new UseEventResult(updated, event);
    }

    /**
     * 적립취소 이벤트 결과
     */
    public record CancelEarnEventResult(MemberPoint memberPoint, PointEarnCanceledEvent event) {}

    /**
     * 적립 취소 처리 → 이벤트 생성
     */
    public CancelEarnEventResult processCancelEarn(UUID ledgerId, long currentVersion) {
        PointLedger targetLedger = findLedgerById(ledgerId);

        // 취소 검증은 PointLedger.cancel()에서 수행
        if (targetLedger.isCanceled()) {
            throw new PointLedgerAlreadyCanceledException(ledgerId);
        }
        if (!targetLedger.canCancel()) {
            throw new PointLedgerAlreadyUsedException(ledgerId);
        }

        PointEarnCanceledEvent event = new PointEarnCanceledEvent(
                UuidGenerator.generate(),
                memberId,
                ledgerId,
                targetLedger.earnedAmount().getValue(),
                currentVersion + 1,
                LocalDateTime.now()
        );

        MemberPoint updated = apply(event);
        return new CancelEarnEventResult(updated, event);
    }

    /**
     * 사용취소 이벤트 결과
     */
    public record CancelUseEventResult(
            MemberPoint memberPoint,
            PointUseCanceledEvent event,
            List<PointUsageDetail> updatedUsageDetails
    ) {}

    /**
     * 사용 취소 처리 → 이벤트 생성
     */
    public CancelUseEventResult processCancelUse(
            List<PointUsageDetail> usageDetails,
            PointAmount cancelAmount,
            int defaultExpirationDays,
            UUID originalTransactionId,
            String orderId,
            long currentVersion
    ) {
        PointAmount totalCancelable = usageDetails.stream()
                .map(PointUsageDetail::getCancelableAmount)
                .reduce(PointAmount.ZERO, PointAmount::add);

        if (cancelAmount.isGreaterThan(totalCancelable)) {
            throw new InvalidCancelAmountException(cancelAmount.getValue(), totalCancelable.getValue());
        }

        Map<UUID, PointLedger> ledgerMap = ledgers.stream()
                .collect(Collectors.toMap(PointLedger::id, l -> l));

        List<PointUseCanceledEvent.RestoredLedger> restoredLedgers = new ArrayList<>();
        List<PointUseCanceledEvent.NewLedger> newLedgers = new ArrayList<>();
        List<PointUsageDetail> updatedUsageDetails = new ArrayList<>();

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
                UUID newLedgerId = UuidGenerator.generate();
                newLedgers.add(new PointUseCanceledEvent.NewLedger(
                        newLedgerId,
                        cancelResult.canceledAmount().getValue(),
                        originalLedger.earnType(),
                        LocalDateTime.now().plusDays(defaultExpirationDays)
                ));
            } else if (originalLedger != null) {
                restoredLedgers.add(new PointUseCanceledEvent.RestoredLedger(
                        originalLedger.id(),
                        cancelResult.canceledAmount().getValue()
                ));
            }
        }

        PointUseCanceledEvent event = new PointUseCanceledEvent(
                UuidGenerator.generate(),
                memberId,
                originalTransactionId,
                cancelAmount.getValue(),
                orderId,
                restoredLedgers,
                newLedgers,
                currentVersion + 1,
                LocalDateTime.now()
        );

        MemberPoint updated = apply(event);
        return new CancelUseEventResult(updated, event, updatedUsageDetails);
    }

    /**
     * 스냅샷 생성
     */
    public MemberPointSnapshot toSnapshot(long version) {
        return MemberPointSnapshot.from(this, version);
    }

    // =====================================================
    // 적립 (Earn) - 기존 State-based 방식 (하위 호환)
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
