package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PointUsagePolicyTest {

    private PointUsagePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new PointUsagePolicy();
    }

    @Nested
    @DisplayName("단일 적립건 사용")
    class SingleLedgerUsageTest {

        @Test
        @DisplayName("부분 사용 시 올바른 금액이 차감된다")
        void partialUse_shouldDeductCorrectAmount() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(ledger), PointAmount.of(500L));

            // THEN
            assertThat(result.updatedLedgers()).hasSize(1);
            assertThat(result.usageDetails()).hasSize(1);
            assertThat(result.usageDetails().get(0).usedAmount().getValue()).isEqualTo(500L);
            assertThat(ledger.getAvailableAmount().getValue()).isEqualTo(500L);
        }

        @Test
        @DisplayName("전액 사용 시 잔액이 0이 된다")
        void fullUse_shouldMakeBalanceZero() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(ledger), PointAmount.of(1000L));

            // THEN
            assertThat(result.usageDetails().get(0).usedAmount().getValue()).isEqualTo(1000L);
            assertThat(ledger.getAvailableAmount().getValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("여러 적립건 사용")
    class MultipleLedgerUsageTest {

        @Test
        @DisplayName("첫 번째 적립건으로 충분하지 않으면 다음 적립건도 사용")
        void multipleUsage_shouldUseNextLedger() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            UUID id1 = UuidGenerator.generate();
            UUID id2 = UuidGenerator.generate();
            PointLedger ledger1 = PointLedgerFixture.createSystem(id1, memberId, 500L);
            PointLedger ledger2 = PointLedgerFixture.createSystem(id2, memberId, 500L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(ledger1, ledger2), PointAmount.of(800L));

            // THEN
            assertThat(result.updatedLedgers()).hasSize(2);
            assertThat(result.usageDetails()).hasSize(2);
            assertThat(result.usageDetails().get(0).usedAmount().getValue()).isEqualTo(500L);
            assertThat(result.usageDetails().get(1).usedAmount().getValue()).isEqualTo(300L);
        }

        @Test
        @DisplayName("필요한 만큼만 적립건 사용")
        void partialUsage_shouldStopWhenEnough() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            UUID id1 = UuidGenerator.generate();
            UUID id2 = UuidGenerator.generate();
            UUID id3 = UuidGenerator.generate();
            PointLedger ledger1 = PointLedgerFixture.createSystem(id1, memberId, 500L);
            PointLedger ledger2 = PointLedgerFixture.createSystem(id2, memberId, 500L);
            PointLedger ledger3 = PointLedgerFixture.createSystem(id3, memberId, 500L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(ledger1, ledger2, ledger3), PointAmount.of(600L));

            // THEN
            assertThat(result.usageDetails()).hasSize(2);
            assertThat(ledger3.getAvailableAmount().getValue()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("사용 우선순위")
    class UsagePriorityTest {

        @Test
        @DisplayName("리스트 순서대로 사용 (정렬은 호출자 책임)")
        void shouldUseInListOrder() {
            // GIVEN - 수기 지급이 먼저 오도록 정렬된 리스트 가정
            UUID memberId = UuidGenerator.generate();
            UUID manualId = UuidGenerator.generate();
            UUID systemId = UuidGenerator.generate();
            PointLedger manual = PointLedgerFixture.createManual(manualId, memberId, 500L);
            PointLedger system = PointLedgerFixture.createSystem(systemId, memberId, 500L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(manual, system), PointAmount.of(300L));

            // THEN
            assertThat(result.usageDetails()).hasSize(1);
            assertThat(result.usageDetails().get(0).ledgerId()).isEqualTo(manualId);
            assertThat(manual.getAvailableAmount().getValue()).isEqualTo(200L);
            assertThat(system.getAvailableAmount().getValue()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("사용 금액이 0이면 아무것도 사용하지 않음")
        void zeroAmount_shouldNotUseAny() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(ledger), PointAmount.ZERO);

            // THEN
            assertThat(result.updatedLedgers()).isEmpty();
            assertThat(result.usageDetails()).isEmpty();
        }

        @Test
        @DisplayName("빈 리스트에서는 아무것도 사용하지 않음")
        void emptyList_shouldNotUseAny() {
            // WHEN
            PointUsagePolicy.UsageResult result = policy.use(List.of(), PointAmount.of(500L));

            // THEN
            assertThat(result.updatedLedgers()).isEmpty();
            assertThat(result.usageDetails()).isEmpty();
        }
    }
}
