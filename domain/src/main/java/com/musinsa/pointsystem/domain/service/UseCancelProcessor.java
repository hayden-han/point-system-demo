package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.IdGenerator;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 포인트 사용취소 처리 도메인 서비스
 * - 사용취소 비즈니스 로직 캡슐화
 * - 순수 도메인 로직만 담당 (인프라 의존성 없음)
 * - Bean 등록은 Application 레이어(DomainServiceConfig)에서 담당
 */
public class UseCancelProcessor {

    private final IdGenerator idGenerator;

    public UseCancelProcessor(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 사용취소 처리 결과
     */
    public record CancelResult(
            List<PointLedger> updatedLedgers,
            List<PointLedger> newLedgers,
            List<LedgerEntry> newEntries,
            long canceledAmount
    ) {
        public static CancelResult empty() {
            return new CancelResult(List.of(), List.of(), List.of(), 0L);
        }
    }

    /**
     * 취소 가능 정보
     */
    public record CancelableInfo(
            PointLedger ledger,
            long cancelableAmount
    ) {}

    /**
     * 취소 가능 금액 계산
     * @param ledgers 원장 목록
     * @param entriesByLedgerId Ledger별 Entry 맵
     * @param orderId 주문 ID
     * @return 취소 가능 정보 목록 및 총 취소 가능 금액
     */
    public CancelableContext calculateCancelableAmount(
            List<PointLedger> ledgers,
            Map<UUID, List<LedgerEntry>> entriesByLedgerId,
            String orderId
    ) {
        List<CancelableInfo> cancelableInfos = new ArrayList<>();
        long totalCancelable = 0;

        for (PointLedger ledger : ledgers) {
            List<LedgerEntry> entries = entriesByLedgerId.getOrDefault(ledger.id(), List.of());
            long cancelable = PointRules.calculateCancelableAmountForLedger(entries, orderId);
            if (cancelable > 0) {
                cancelableInfos.add(new CancelableInfo(ledger, cancelable));
                totalCancelable += cancelable;
            }
        }

        return new CancelableContext(cancelableInfos, totalCancelable);
    }

    /**
     * 취소 가능 컨텍스트
     */
    public record CancelableContext(
            List<CancelableInfo> cancelableInfos,
            long totalCancelable
    ) {}

    /**
     * 사용취소 처리 실행
     * @param cancelableInfos 취소 가능 정보 목록
     * @param cancelAmount 취소 금액
     * @param memberId 회원 ID
     * @param orderId 주문 ID
     * @param expirationDays 만료 정책 (신규 Ledger 생성 시 사용)
     * @param now 현재 시간
     * @return 처리 결과
     */
    public CancelResult process(
            List<CancelableInfo> cancelableInfos,
            long cancelAmount,
            UUID memberId,
            String orderId,
            int expirationDays,
            LocalDateTime now
    ) {
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<PointLedger> newLedgers = new ArrayList<>();
        List<LedgerEntry> newEntries = new ArrayList<>();
        long remainingCancelAmount = cancelAmount;

        for (CancelableInfo info : cancelableInfos) {
            if (remainingCancelAmount <= 0) break;

            PointLedger ledger = info.ledger();
            long cancelAmountForLedger = Math.min(remainingCancelAmount, info.cancelableAmount());
            remainingCancelAmount -= cancelAmountForLedger;

            // USE_CANCEL Entry 생성
            LedgerEntry cancelEntry = LedgerEntry.createUseCancel(
                    idGenerator.generate(),
                    ledger.id(),
                    cancelAmountForLedger,
                    orderId,
                    now
            );
            newEntries.add(cancelEntry);

            // Ledger가 만료되었는지 확인하여 분기 처리
            boolean isExpired = PointRules.isExpired(ledger, now);

            if (isExpired) {
                // 만료된 경우: 신규 Ledger 생성
                PointLedger newLedger = createNewLedgerForExpired(
                        ledger, cancelAmountForLedger, memberId, expirationDays, now);
                newLedgers.add(newLedger);

                // 신규 Ledger에 대한 EARN Entry 생성
                LedgerEntry earnEntry = LedgerEntry.createEarn(
                        idGenerator.generate(),
                        newLedger.id(),
                        cancelAmountForLedger,
                        now
                );
                newEntries.add(earnEntry);
            } else {
                // 만료되지 않은 경우: 기존 Ledger 복원
                PointLedger updatedLedger = ledger.withAvailableAmount(
                        ledger.availableAmount() + cancelAmountForLedger);
                updatedLedgers.add(updatedLedger);
            }
        }

        return new CancelResult(updatedLedgers, newLedgers, newEntries, cancelAmount);
    }

    private PointLedger createNewLedgerForExpired(
            PointLedger originalLedger,
            long amount,
            UUID memberId,
            int expirationDays,
            LocalDateTime now
    ) {
        LocalDateTime newExpiredAt = PointRules.calculateNewExpirationDate(now, expirationDays);

        return PointLedger.create(
                idGenerator.generate(),
                memberId,
                amount,
                EarnType.USE_CANCEL,
                newExpiredAt,
                originalLedger.id(),  // sourceLedgerId
                now
        );
    }
}
