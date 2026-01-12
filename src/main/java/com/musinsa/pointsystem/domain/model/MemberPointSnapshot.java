package com.musinsa.pointsystem.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MemberPoint Aggregate 스냅샷
 * - 이벤트 리플레이 성능 최적화를 위한 상태 저장
 * - 주기적으로 생성 (예: 100개 이벤트마다)
 */
public record MemberPointSnapshot(
        UUID memberId,
        long totalBalance,
        List<LedgerSnapshot> ledgers,
        long version,
        LocalDateTime createdAt
) {

    /**
     * 적립건 스냅샷
     */
    public record LedgerSnapshot(
            UUID id,
            long earnedAmount,
            long availableAmount,
            long usedAmount,
            EarnType earnType,
            UUID sourceTransactionId,
            LocalDateTime expiredAt,
            boolean canceled,
            LocalDateTime createdAt
    ) {
        /**
         * PointLedger로부터 스냅샷 생성
         */
        public static LedgerSnapshot from(PointLedger ledger) {
            return new LedgerSnapshot(
                    ledger.id(),
                    ledger.earnedAmount().getValue(),
                    ledger.availableAmount().getValue(),
                    ledger.usedAmount().getValue(),
                    ledger.earnType(),
                    ledger.sourceTransactionId(),
                    ledger.expiredAt(),
                    ledger.canceled(),
                    ledger.createdAt()
            );
        }

        /**
         * 스냅샷으로부터 PointLedger 복원
         */
        public PointLedger toLedger(UUID memberId) {
            return new PointLedger(
                    id,
                    memberId,
                    PointAmount.of(earnedAmount),
                    PointAmount.of(availableAmount),
                    PointAmount.of(usedAmount),
                    earnType,
                    sourceTransactionId,
                    expiredAt,
                    canceled,
                    createdAt
            );
        }
    }

    /**
     * MemberPoint로부터 스냅샷 생성
     */
    public static MemberPointSnapshot from(MemberPoint memberPoint, long version) {
        List<LedgerSnapshot> ledgerSnapshots = memberPoint.ledgers().stream()
                .map(LedgerSnapshot::from)
                .toList();

        return new MemberPointSnapshot(
                memberPoint.memberId(),
                memberPoint.totalBalance().getValue(),
                ledgerSnapshots,
                version,
                LocalDateTime.now()
        );
    }

    /**
     * 스냅샷으로부터 MemberPoint 복원
     */
    public MemberPoint toMemberPoint() {
        List<PointLedger> ledgerList = ledgers.stream()
                .map(snapshot -> snapshot.toLedger(memberId))
                .toList();

        return new MemberPoint(
                memberId,
                PointAmount.of(totalBalance),
                ledgerList
        );
    }
}
