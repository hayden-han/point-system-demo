package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Getter
public class MemberPoint {
    private final UUID memberId;
    private PointAmount totalBalance;
    private List<PointLedger> ledgers;

    @Builder
    public MemberPoint(UUID memberId, PointAmount totalBalance, List<PointLedger> ledgers) {
        this.memberId = memberId;
        this.totalBalance = totalBalance != null ? totalBalance : PointAmount.ZERO;
        this.ledgers = ledgers != null ? new ArrayList<>(ledgers) : new ArrayList<>();
    }

    public static MemberPoint create(UUID memberId) {
        return MemberPoint.builder()
                .memberId(memberId)
                .totalBalance(PointAmount.ZERO)
                .ledgers(new ArrayList<>())
                .build();
    }

    // =====================================================
    // 적립 (Earn)
    // =====================================================

    /**
     * 포인트 적립
     * @param amount 적립 금액
     * @param earnType 적립 유형 (MANUAL/SYSTEM)
     * @param expiredAt 만료일시
     * @param policy 적립 정책
     * @return 생성된 PointLedger
     */
    public PointLedger earn(PointAmount amount, EarnType earnType,
                           LocalDateTime expiredAt, EarnPolicyConfig policy) {
        validateEarnAmount(amount, policy);
        validateMaxBalance(amount, policy);

        PointLedger ledger = PointLedger.create(memberId, amount, earnType, expiredAt);
        this.ledgers.add(ledger);
        this.totalBalance = this.totalBalance.add(amount);
        return ledger;
    }

    /**
     * 만료일 검증 포함 적립
     */
    public PointLedger earnWithExpirationValidation(PointAmount amount, EarnType earnType,
                                                     Integer expirationDays, EarnPolicyConfig policy) {
        validateEarnAmount(amount, policy);
        validateExpirationDays(expirationDays, policy);
        validateMaxBalance(amount, policy);

        LocalDateTime expiredAt = policy.calculateExpirationDate(expirationDays);
        PointLedger ledger = PointLedger.create(memberId, amount, earnType, expiredAt);
        this.ledgers.add(ledger);
        this.totalBalance = this.totalBalance.add(amount);
        return ledger;
    }

    // =====================================================
    // 적립취소 (Cancel Earn)
    // =====================================================

    /**
     * 적립 취소
     * @param ledgerId 취소할 적립건 ID
     * @return 취소된 금액
     * @throws PointLedgerNotFoundException 적립건을 찾을 수 없는 경우
     * @throws PointLedgerAlreadyCanceledException 이미 취소된 경우
     * @throws PointLedgerAlreadyUsedException 사용된 경우
     */
    public PointAmount cancelEarn(UUID ledgerId) {
        PointLedger ledger = findLedgerById(ledgerId);
        PointAmount canceledAmount = ledger.getEarnedAmount();
        ledger.cancel();  // 내부에서 검증
        this.totalBalance = this.totalBalance.subtract(canceledAmount);
        return canceledAmount;
    }

    // =====================================================
    // 사용 (Use)
    // =====================================================

    /**
     * 사용 결과
     */
    public record UsageResult(List<UsageDetail> usageDetails) {}

    /**
     * 사용 상세
     */
    public record UsageDetail(UUID ledgerId, PointAmount usedAmount) {}

    /**
     * 포인트 사용
     * @param amount 사용할 금액
     * @return 사용 결과 (어떤 적립건에서 얼마 사용했는지)
     * @throws InsufficientPointException 잔액 부족 시
     */
    public UsageResult use(PointAmount amount) {
        if (this.totalBalance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), totalBalance.getValue());
        }

        List<PointLedger> availableLedgers = getAvailableLedgersSorted();
        List<UsageDetail> usageDetails = deductFromLedgers(availableLedgers, amount);

        this.totalBalance = this.totalBalance.subtract(amount);
        return new UsageResult(usageDetails);
    }

    // =====================================================
    // 사용취소 (Cancel Use)
    // =====================================================

    /**
     * 복구 결과
     */
    public record RestoreResult(
            List<PointLedger> restoredLedgers,
            List<NewLedgerInfo> newLedgers,
            List<PointUsageDetail> updatedUsageDetails
    ) {}

    /**
     * 신규 적립건 정보 (만료된 적립건 사용취소 시)
     */
    public record NewLedgerInfo(
            PointAmount amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID relatedTransactionId
    ) {}

    /**
     * 사용 취소
     * @param usageDetails 취소 대상 사용 상세 목록 (만료일 긴 것부터 정렬됨)
     * @param cancelAmount 취소할 금액
     * @param defaultExpirationDays 신규 적립 시 기본 만료일
     * @param cancelTransactionId 취소 트랜잭션 ID
     * @return 복구 결과
     * @throws InvalidCancelAmountException 취소 금액이 취소 가능 금액 초과 시
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

        List<PointLedger> restoredLedgers = new ArrayList<>();
        List<NewLedgerInfo> newLedgers = new ArrayList<>();
        List<PointUsageDetail> updatedUsageDetails = new ArrayList<>();

        PointAmount remainingCancelAmount = cancelAmount;

        // 적립건 ID → 적립건 매핑 생성
        Map<UUID, PointLedger> ledgerMap = ledgers.stream()
                .collect(Collectors.toMap(PointLedger::getId, l -> l));

        for (PointUsageDetail usageDetail : usageDetails) {
            if (remainingCancelAmount.isZero()) {
                break;
            }

            PointAmount cancelFromDetail = usageDetail.cancel(remainingCancelAmount);
            remainingCancelAmount = remainingCancelAmount.subtract(cancelFromDetail);
            updatedUsageDetails.add(usageDetail);

            PointLedger originalLedger = ledgerMap.get(usageDetail.getLedgerId());

            if (originalLedger != null && originalLedger.isExpired()) {
                // 만료된 적립건 → 신규 적립 정보 생성
                newLedgers.add(new NewLedgerInfo(
                        cancelFromDetail,
                        originalLedger.getEarnType(),
                        LocalDateTime.now().plusDays(defaultExpirationDays),
                        cancelTransactionId
                ));
            } else if (originalLedger != null) {
                // 만료되지 않은 적립건 → 복구
                originalLedger.restore(cancelFromDetail);
                restoredLedgers.add(originalLedger);
            }
        }

        // 신규 적립건 생성하여 ledgers에 추가
        for (NewLedgerInfo newLedgerInfo : newLedgers) {
            PointLedger newLedger = PointLedger.createFromCancelUse(
                    memberId,
                    newLedgerInfo.amount(),
                    newLedgerInfo.earnType(),
                    newLedgerInfo.expiredAt(),
                    newLedgerInfo.relatedTransactionId()
            );
            this.ledgers.add(newLedger);
        }

        this.totalBalance = this.totalBalance.add(cancelAmount);
        return new RestoreResult(restoredLedgers, newLedgers, updatedUsageDetails);
    }

    // =====================================================
    // 기존 메서드 (호환성 유지)
    // =====================================================

    public void increaseBalance(PointAmount amount) {
        this.totalBalance = this.totalBalance.add(amount);
    }

    /**
     * 잔액 차감 (직접 호출 - 호환성 유지용)
     */
    public void decreaseBalance(PointAmount amount) {
        if (this.totalBalance.isLessThan(amount)) {
            throw new InsufficientPointException(amount.getValue(), this.totalBalance.getValue());
        }
        this.totalBalance = this.totalBalance.subtract(amount);
    }

    public boolean canEarn(PointAmount amount, PointAmount maxBalance) {
        return this.totalBalance.add(amount).isLessThanOrEqual(maxBalance);
    }

    public boolean hasEnoughBalance(PointAmount amount) {
        return this.totalBalance.isGreaterThanOrEqual(amount);
    }

    // =====================================================
    // 내부 헬퍼 메서드
    // =====================================================

    /**
     * 사용 가능한 적립건을 우선순위에 따라 정렬하여 반환
     * 우선순위: MANUAL 우선, 만료일 짧은 순
     */
    private List<PointLedger> getAvailableLedgersSorted() {
        return ledgers.stream()
                .filter(PointLedger::isAvailable)
                .sorted(comparing(PointLedger::isManual).reversed()
                        .thenComparing(PointLedger::getExpiredAt))
                .toList();
    }

    /**
     * 적립건들에서 금액 차감
     */
    private List<UsageDetail> deductFromLedgers(List<PointLedger> availableLedgers, PointAmount amount) {
        List<UsageDetail> usageDetails = new ArrayList<>();
        PointAmount remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount.isZero()) {
                break;
            }

            PointAmount usedFromLedger = ledger.use(remainingAmount);
            remainingAmount = remainingAmount.subtract(usedFromLedger);
            usageDetails.add(new UsageDetail(ledger.getId(), usedFromLedger));
        }

        return usageDetails;
    }

    /**
     * ID로 적립건 찾기
     */
    public PointLedger findLedgerById(UUID ledgerId) {
        return ledgers.stream()
                .filter(l -> l.getId().equals(ledgerId))
                .findFirst()
                .orElseThrow(() -> new PointLedgerNotFoundException(ledgerId));
    }

    /**
     * 적립건 추가 (외부에서 생성된 적립건)
     */
    public void addLedger(PointLedger ledger) {
        this.ledgers.add(ledger);
    }

    // =====================================================
    // 검증 메서드 (PointEarnValidator에서 이동)
    // =====================================================

    private void validateEarnAmount(PointAmount amount, EarnPolicyConfig policy) {
        if (amount.isLessThan(policy.getMinAmount())) {
            throw InvalidEarnAmountException.belowMinimum(amount.getValue(), policy.getMinAmount().getValue());
        }
        if (amount.isGreaterThan(policy.getMaxAmount())) {
            throw InvalidEarnAmountException.aboveMaximum(amount.getValue(), policy.getMaxAmount().getValue());
        }
    }

    private void validateMaxBalance(PointAmount earnAmount, EarnPolicyConfig policy) {
        if (!canEarn(earnAmount, policy.getMaxBalance())) {
            throw new MaxBalanceExceededException(
                    this.totalBalance.getValue(), earnAmount.getValue(), policy.getMaxBalance().getValue());
        }
    }

    private void validateExpirationDays(Integer expirationDays, EarnPolicyConfig policy) {
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
