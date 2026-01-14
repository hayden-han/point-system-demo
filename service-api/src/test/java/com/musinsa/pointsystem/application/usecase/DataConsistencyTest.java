package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.LedgerEntryRepository;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터 정합성 검증 테스트 ((LedgerEntry 기반))
 * - LedgerEntry 기반 검증
 */
class DataConsistencyTest extends IntegrationTestBase {

    @Autowired
    private MemberPointRepository memberPointRepository;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Nested
    @DisplayName("정합성 검증 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/validation-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/validation-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class ConsistencyCases {

        @Test
        @DisplayName("V-T01: 잔액 정합성 - getTotalBalance(now) == SUM(valid ledger.available_amount)")
        void balanceConsistency_shouldMatch() {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000007001");
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            MemberPoint memberPoint = memberPointRepository.findByMemberIdWithAllLedgersAndEntries(memberId).orElseThrow();
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            long sumOfAvailable = availableLedgers.stream()
                    .map(ledger -> ledger.availableAmount().getValue())
                    .mapToLong(v -> v)
                    .sum();

            // THEN
            PointAmount totalBalance = memberPoint.getTotalBalance(now);
            assertThat(totalBalance).isEqualTo(PointAmount.of(1500L));
            assertThat(sumOfAvailable).isEqualTo(totalBalance.getValue());
            // 유효한 적립건은 2개 (7001: 800, 7002: 700)
            assertThat(availableLedgers).hasSize(2);
        }

        @Test
        @DisplayName("V-T02: 적립건 정합성 - earned_amount == available_amount + usedAmount()")
        void ledgerConsistency_shouldMatch() {
            // GIVEN
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000007005");

            // WHEN (entries 포함 조회)
            PointLedger ledger = pointLedgerRepository.findByIdWithEntries(ledgerId).orElseThrow();

            // THEN
            assertThat(ledger.earnedAmount()).isEqualTo(PointAmount.of(1000L));
            assertThat(ledger.availableAmount()).isEqualTo(PointAmount.of(500L));
            assertThat(ledger.usedAmount()).isEqualTo(PointAmount.of(500L));
            assertThat(ledger.earnedAmount().getValue())
                    .isEqualTo(ledger.availableAmount().getValue() + ledger.usedAmount().getValue());
        }

        @Test
        @DisplayName("V-T03: Entry 기반 사용 정합성 - 같은 orderId의 USE Entry 합계 == 실제 사용금액")
        void entryUsageConsistency_shouldMatch() {
            // GIVEN - ORDER-V-T03로 두 적립건에서 각각 500씩 총 1000원 사용
            String orderId = "ORDER-V-T03";

            // WHEN
            List<LedgerEntry> entries = ledgerEntryRepository.findByOrderId(orderId);
            long sumOfUse = entries.stream()
                    .filter(e -> e.type() == EntryType.USE)
                    .mapToLong(e -> Math.abs(e.amount()))
                    .sum();

            // THEN
            assertThat(sumOfUse).isEqualTo(1000L);
            // 두 적립건에서 각각 500씩 USE Entry
            long useEntryCount = entries.stream()
                    .filter(e -> e.type() == EntryType.USE)
                    .count();
            assertThat(useEntryCount).isEqualTo(2);
        }

        @Test
        @DisplayName("V-T04: Entry 기반 취소 정합성 - USE_CANCEL 합계 <= USE 합계")
        void entryCancelConsistency_shouldBeValid() {
            // GIVEN - ORDER-V-T04로 1000원 사용, 300원 취소
            String orderId = "ORDER-V-T04";

            // WHEN
            List<LedgerEntry> entries = ledgerEntryRepository.findByOrderId(orderId);
            long sumOfUse = entries.stream()
                    .filter(e -> e.type() == EntryType.USE)
                    .mapToLong(e -> Math.abs(e.amount()))
                    .sum();
            long sumOfCancel = entries.stream()
                    .filter(e -> e.type() == EntryType.USE_CANCEL)
                    .mapToLong(LedgerEntry::amount)
                    .sum();

            // THEN
            assertThat(sumOfUse).isEqualTo(1000L);
            assertThat(sumOfCancel).isEqualTo(300L);
            assertThat(sumOfCancel).isLessThanOrEqualTo(sumOfUse);
            // 취소 가능 금액 = 사용 - 취소 = 700
            assertThat(sumOfUse - sumOfCancel).isEqualTo(700L);
        }
    }
}
