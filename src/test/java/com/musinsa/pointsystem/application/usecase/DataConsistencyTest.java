package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.domain.model.MemberPoint;
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
            Long memberId = 7001L;

            // WHEN
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId).orElseThrow();
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            Long sumOfAvailable = availableLedgers.stream()
                    .mapToLong(PointLedger::getAvailableAmount)
                    .sum();

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(1500L);
            assertThat(sumOfAvailable).isEqualTo(memberPoint.getTotalBalance());
            // 유효한 적립건은 2개 (7001: 800, 7002: 700)
            assertThat(availableLedgers).hasSize(2);
        }

        @Test
        @DisplayName("V-T02: 적립건 정합성 - earned_amount == available_amount + used_amount")
        void ledgerConsistency_shouldMatch() {
            // GIVEN
            Long ledgerId = 7005L;

            // WHEN
            PointLedger ledger = pointLedgerRepository.findById(ledgerId).orElseThrow();

            // THEN
            assertThat(ledger.getEarnedAmount()).isEqualTo(1000L);
            assertThat(ledger.getAvailableAmount()).isEqualTo(500L);
            assertThat(ledger.getUsedAmount()).isEqualTo(500L);
            assertThat(ledger.getEarnedAmount())
                    .isEqualTo(ledger.getAvailableAmount() + ledger.getUsedAmount());
        }

        @Test
        @DisplayName("V-T03: 사용상세 정합성 - SUM(usage_detail.used_amount - canceled_amount) == 실제 사용금액")
        void usageDetailConsistency_shouldMatch() {
            // GIVEN
            Long transactionId = 7006L;

            // WHEN
            List<PointUsageDetail> usageDetails = pointUsageDetailRepository.findByTransactionId(transactionId);
            Long sumOfUsed = usageDetails.stream()
                    .mapToLong(PointUsageDetail::getUsedAmount)
                    .sum();
            Long sumOfCanceled = usageDetails.stream()
                    .mapToLong(PointUsageDetail::getCanceledAmount)
                    .sum();

            // THEN
            assertThat(sumOfUsed).isEqualTo(1000L);
            assertThat(sumOfCanceled).isEqualTo(0L);
            // 두 적립건에서 각각 500씩 사용
            assertThat(usageDetails).hasSize(2);
            usageDetails.forEach(detail ->
                assertThat(detail.getUsedAmount()).isEqualTo(500L)
            );
        }

        @Test
        @DisplayName("V-T04: 취소 정합성 - canceled_amount <= used_amount")
        void cancelConsistency_shouldBeValid() {
            // GIVEN
            Long transactionId = 7008L;

            // WHEN
            List<PointUsageDetail> usageDetails = pointUsageDetailRepository.findByTransactionId(transactionId);

            // THEN
            assertThat(usageDetails).hasSize(1);
            PointUsageDetail detail = usageDetails.get(0);

            assertThat(detail.getUsedAmount()).isEqualTo(1000L);
            assertThat(detail.getCanceledAmount()).isEqualTo(300L);
            assertThat(detail.getCanceledAmount()).isLessThanOrEqualTo(detail.getUsedAmount());
            assertThat(detail.getCancelableAmount()).isEqualTo(700L);
        }
    }
}
