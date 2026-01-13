package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 적립건 (Entity)
 * - 불변 record + wither 패턴
 * - 상태 변경 시 새 객체 반환
 * - LedgerEntry를 내부에 포함
 */
public record PointLedger(
        UUID id,
        UUID memberId,
        PointAmount earnedAmount,
        PointAmount availableAmount,
        EarnType earnType,
        UUID sourceLedgerId,
        LocalDateTime expiredAt,
        boolean canceled,
        LocalDateTime earnedAt,
        List<LedgerEntry> entries
) {
    public PointLedger {
        if (availableAmount == null) {
            availableAmount = PointAmount.ZERO;
        }
        entries = entries != null ? List.copyOf(entries) : List.of();
    }

    /**
     * 적립건 생성 - 최초 EARN Entry 포함
     */
    public static PointLedger create(
            UUID id,
            UUID memberId,
            PointAmount amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID sourceLedgerId,
            IdGenerator idGenerator,
            LocalDateTime now
    ) {
        LedgerEntry earnEntry = LedgerEntry.createEarn(
                idGenerator.generate(),
                amount.getValue(),
                now
        );

        return new PointLedger(
                id, memberId, amount, amount, earnType, sourceLedgerId,
                expiredAt, false, now, List.of(earnEntry)
        );
    }

    /**
     * 레거시 호환을 위한 생성자 (entries 없이 생성)
     * @deprecated entries를 포함하는 create() 메서드 사용 권장
     */
    @Deprecated
    public static PointLedger createLegacy(
            UUID id,
            UUID memberId,
            PointAmount amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID sourceLedgerId,
            LocalDateTime earnedAt
    ) {
        return new PointLedger(
                id, memberId, amount, amount, earnType, sourceLedgerId,
                expiredAt, false, earnedAt, List.of()
        );
    }

    // =====================================================
    // 비즈니스 메서드
    // =====================================================

    public boolean canCancel() {
        return !canceled && earnedAmount.equals(availableAmount);
    }

    /**
     * 만료 여부 확인 (주어진 시간 기준)
     */
    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    /**
     * 만료 여부 확인 (현재 시간 기준 - UTC)
     * @deprecated TimeProvider를 통한 isExpired(LocalDateTime) 사용 권장
     */
    @Deprecated
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now(java.time.ZoneOffset.UTC));
    }

    /**
     * 사용 가능 여부 (주어진 시간 기준)
     */
    public boolean isAvailable(LocalDateTime now) {
        return !canceled && !isExpired(now) && availableAmount.isPositive();
    }

    /**
     * 사용 가능 여부 (현재 시간 기준 - UTC)
     * @deprecated TimeProvider를 통한 isAvailable(LocalDateTime) 사용 권장
     */
    @Deprecated
    public boolean isAvailable() {
        return !canceled && !isExpired() && availableAmount.isPositive();
    }

    public boolean isManual() {
        return earnType == EarnType.MANUAL;
    }

    // =====================================================
    // 적립 취소
    // =====================================================

    /**
     * 적립 취소 수행 - EARN_CANCEL Entry 추가
     */
    public CancelResult cancel(IdGenerator idGenerator, LocalDateTime now) {
        if (canceled) {
            throw new PointLedgerAlreadyCanceledException(id);
        }
        if (!earnedAmount.equals(availableAmount)) {
            throw new PointLedgerAlreadyUsedException(id);
        }

        LedgerEntry cancelEntry = LedgerEntry.createEarnCancel(
                idGenerator.generate(),
                earnedAmount.getValue(),
                now
        );

        List<LedgerEntry> newEntries = new ArrayList<>(entries);
        newEntries.add(cancelEntry);

        PointLedger updated = new PointLedger(
                id, memberId, earnedAmount, PointAmount.ZERO,
                earnType, sourceLedgerId, expiredAt, true, earnedAt, newEntries
        );

        return new CancelResult(updated, earnedAmount);
    }

    /**
     * 적립취소 결과
     */
    public record CancelResult(PointLedger ledger, PointAmount canceledAmount) {}

    // =====================================================
    // 사용
    // =====================================================

    /**
     * 사용 처리 - USE Entry 추가
     */
    public UseResult use(PointAmount amount, String orderId, IdGenerator idGenerator, LocalDateTime now) {
        PointAmount useAmount = amount.min(availableAmount);

        LedgerEntry useEntry = LedgerEntry.createUse(
                idGenerator.generate(),
                useAmount.getValue(),
                orderId,
                now
        );

        List<LedgerEntry> newEntries = new ArrayList<>(entries);
        newEntries.add(useEntry);

        PointLedger updated = new PointLedger(
                id, memberId, earnedAmount, availableAmount.subtract(useAmount),
                earnType, sourceLedgerId, expiredAt, canceled, earnedAt, newEntries
        );

        return new UseResult(updated, useAmount);
    }

    /**
     * 사용 결과
     */
    public record UseResult(PointLedger ledger, PointAmount usedAmount) {}

    // =====================================================
    // 사용 취소
    // =====================================================

    /**
     * 사용취소 처리 - USE_CANCEL Entry 추가
     * @param orderId 취소할 주문 ID
     * @param cancelAmount 취소 요청 금액
     * @param now 현재 시간
     * @param idGenerator ID 생성기
     * @param defaultExpirationDays 만료 시 신규 적립건 유효기간
     */
    public CancelUseResult cancelUse(
            String orderId,
            PointAmount cancelAmount,
            LocalDateTime now,
            IdGenerator idGenerator,
            int defaultExpirationDays
    ) {
        PointAmount cancelable = getCancelableAmountByOrder(orderId);
        PointAmount actualCancel = cancelAmount.min(cancelable);

        if (actualCancel.isZero()) {
            return new CancelUseResult(this, PointAmount.ZERO, null);
        }

        LedgerEntry cancelEntry = LedgerEntry.createUseCancel(
                idGenerator.generate(),
                actualCancel.getValue(),
                orderId,
                now
        );

        List<LedgerEntry> newEntries = new ArrayList<>(entries);
        newEntries.add(cancelEntry);

        if (isExpired(now)) {
            // 만료됨 → availableAmount 변경 없이 Entry만 추가, 신규 Ledger 생성
            PointLedger updated = new PointLedger(
                    id, memberId, earnedAmount, availableAmount,
                    earnType, sourceLedgerId, expiredAt, canceled, earnedAt, newEntries
            );

            PointLedger newLedger = PointLedger.create(
                    idGenerator.generate(),
                    memberId,
                    actualCancel,
                    earnType,
                    now.plusDays(defaultExpirationDays),
                    this.id,
                    idGenerator,
                    now
            );

            return new CancelUseResult(updated, actualCancel, newLedger);
        } else {
            // 만료 안됨 → availableAmount 복구
            PointLedger updated = new PointLedger(
                    id, memberId, earnedAmount, availableAmount.add(actualCancel),
                    earnType, sourceLedgerId, expiredAt, canceled, earnedAt, newEntries
            );

            return new CancelUseResult(updated, actualCancel, null);
        }
    }

    /**
     * 해당 주문에서 취소 가능한 금액
     * - USE는 음수, USE_CANCEL은 양수 → 합산 후 부호 반전
     */
    public PointAmount getCancelableAmountByOrder(String orderId) {
        long netAmount = entries.stream()
                .filter(e -> orderId != null && orderId.equals(e.orderId()))
                .filter(e -> e.type() == EntryType.USE || e.type() == EntryType.USE_CANCEL)
                .mapToLong(LedgerEntry::amount)
                .sum();

        // netAmount는 음수(사용 > 취소) 또는 0 → 절대값으로 취소 가능 금액 반환
        return PointAmount.of(Math.abs(Math.min(netAmount, 0)));
    }

    /**
     * 사용취소 결과
     */
    public record CancelUseResult(PointLedger ledger, PointAmount canceledAmount, PointLedger newLedger) {}

    // =====================================================
    // 레거시 호환 메서드 (deprecated)
    // =====================================================

    /**
     * @deprecated use(PointAmount, String, IdGenerator, LocalDateTime) 사용
     */
    @Deprecated
    public UseResult use(PointAmount amount) {
        PointAmount useAmount = amount.min(availableAmount);
        PointLedger updated = new PointLedger(
                id, memberId, earnedAmount, availableAmount.subtract(useAmount),
                earnType, sourceLedgerId, expiredAt, canceled, earnedAt, entries
        );
        return new UseResult(updated, useAmount);
    }

    /**
     * @deprecated cancel(IdGenerator, LocalDateTime) 사용
     */
    @Deprecated
    public PointLedger cancel() {
        if (canceled) {
            throw new PointLedgerAlreadyCanceledException(id);
        }
        if (!earnedAmount.equals(availableAmount)) {
            throw new PointLedgerAlreadyUsedException(id);
        }
        return new PointLedger(
                id, memberId, earnedAmount, PointAmount.ZERO,
                earnType, sourceLedgerId, expiredAt, true, earnedAt, entries
        );
    }

    /**
     * @deprecated cancelUse() 메서드 사용
     */
    @Deprecated
    public PointLedger restore(PointAmount amount) {
        return new PointLedger(
                id, memberId, earnedAmount, availableAmount.add(amount),
                earnType, sourceLedgerId, expiredAt, canceled, earnedAt, entries
        );
    }

    // =====================================================
    // 헬퍼 메서드
    // =====================================================

    /**
     * 사용된 금액 (entries 기반 계산)
     */
    public PointAmount usedAmount() {
        long used = entries.stream()
                .filter(e -> e.type() == EntryType.USE)
                .mapToLong(e -> Math.abs(e.amount()))
                .sum();

        long canceled = entries.stream()
                .filter(e -> e.type() == EntryType.USE_CANCEL)
                .mapToLong(LedgerEntry::amount)
                .sum();

        return PointAmount.of(used - canceled);
    }

    /**
     * entries 변경 시 새 객체 반환
     */
    public PointLedger withEntries(List<LedgerEntry> newEntries) {
        return new PointLedger(
                id, memberId, earnedAmount, availableAmount,
                earnType, sourceLedgerId, expiredAt, canceled, earnedAt, newEntries
        );
    }
}
