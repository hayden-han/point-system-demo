package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataConsistencyTest extends IntegrationTestBase {

    @Autowired
    private MemberPointRepository memberPointRepository;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Autowired
    private PointUsageDetailRepository pointUsageDetailRepository;

    @Nested
    @DisplayName("정합성 검증 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/validation-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/validation-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class ConsistencyCases {

        @Test
        @DisplayName("V-T01: 잔액 정합성 - member_point.total_balance == SUM(valid ledger.available_amount)")
        void balanceConsistency_shouldMatch() {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000007001");

            // WHEN
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId).orElseThrow();
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            long sumOfAvailable = availableLedgers.stream()
                    .map(ledger -> ledger.availableAmount().getValue())
                    .mapToLong(v -> v)
                    .sum();

            // THEN
            assertThat(memberPoint.totalBalance()).isEqualTo(PointAmount.of(1500L));
            assertThat(sumOfAvailable).isEqualTo(memberPoint.totalBalance().getValue());
            // 유효한 적립건은 2개 (7001: 800, 7002: 700)
            assertThat(availableLedgers).hasSize(2);
        }

        @Test
        @DisplayName("V-T02: 적립건 정합성 - earned_amount == available_amount + used_amount")
        void ledgerConsistency_shouldMatch() {
            // GIVEN
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000007005");

            // WHEN
            PointLedger ledger = pointLedgerRepository.findById(ledgerId).orElseThrow();

            // THEN
            assertThat(ledger.earnedAmount()).isEqualTo(PointAmount.of(1000L));
            assertThat(ledger.availableAmount()).isEqualTo(PointAmount.of(500L));
            assertThat(ledger.usedAmount()).isEqualTo(PointAmount.of(500L));
            assertThat(ledger.earnedAmount().getValue())
                    .isEqualTo(ledger.availableAmount().getValue() + ledger.usedAmount().getValue());
        }

        @Test
        @DisplayName("V-T03: 사용상세 정합성 - SUM(usage_detail.used_amount - canceled_amount) == 실제 사용금액")
        void usageDetailConsistency_shouldMatch() {
            // GIVEN
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000007006");

            // WHEN
            List<PointUsageDetail> usageDetails = pointUsageDetailRepository.findByTransactionId(transactionId);
            long sumOfUsed = usageDetails.stream()
                    .map(detail -> detail.usedAmount().getValue())
                    .mapToLong(v -> v)
                    .sum();
            long sumOfCanceled = usageDetails.stream()
                    .map(detail -> detail.canceledAmount().getValue())
                    .mapToLong(v -> v)
                    .sum();

            // THEN
            assertThat(sumOfUsed).isEqualTo(1000L);
            assertThat(sumOfCanceled).isEqualTo(0L);
            // 두 적립건에서 각각 500씩 사용
            assertThat(usageDetails).hasSize(2);
            usageDetails.forEach(detail ->
                assertThat(detail.usedAmount()).isEqualTo(PointAmount.of(500L))
            );
        }

        @Test
        @DisplayName("V-T04: 취소 정합성 - canceled_amount <= used_amount")
        void cancelConsistency_shouldBeValid() {
            // GIVEN
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000007008");

            // WHEN
            List<PointUsageDetail> usageDetails = pointUsageDetailRepository.findByTransactionId(transactionId);

            // THEN
            assertThat(usageDetails).hasSize(1);
            PointUsageDetail detail = usageDetails.get(0);

            assertThat(detail.usedAmount()).isEqualTo(PointAmount.of(1000L));
            assertThat(detail.canceledAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(detail.canceledAmount().getValue()).isLessThanOrEqualTo(detail.usedAmount().getValue());
            assertThat(detail.getCancelableAmount()).isEqualTo(PointAmount.of(700L));
        }
    }
}
