package com.musinsa.pointsystem.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LedgerEntry 단위 테스트")
class LedgerEntryTest {

    @Nested
    @DisplayName("EARN Entry 생성")
    class CreateEarnTest {

        @Test
        @DisplayName("EARN Entry는 양수 금액으로 생성됨")
        void createEarn_shouldHavePositiveAmount() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createEarn(id, ledgerId, 1000L, now);

            // THEN
            assertThat(entry.id()).isEqualTo(id);
            assertThat(entry.ledgerId()).isEqualTo(ledgerId);
            assertThat(entry.type()).isEqualTo(EntryType.EARN);
            assertThat(entry.amount()).isEqualTo(1000L);
            assertThat(entry.orderId()).isNull();
            assertThat(entry.createdAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("EARN_CANCEL Entry 생성")
    class CreateEarnCancelTest {

        @Test
        @DisplayName("EARN_CANCEL Entry는 음수 금액으로 생성됨")
        void createEarnCancel_shouldHaveNegativeAmount() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createEarnCancel(id, ledgerId, 1000L, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.EARN_CANCEL);
            assertThat(entry.amount()).isEqualTo(-1000L);
            assertThat(entry.absoluteAmount()).isEqualTo(1000L);
            assertThat(entry.orderId()).isNull();
        }
    }

    @Nested
    @DisplayName("USE Entry 생성")
    class CreateUseTest {

        @Test
        @DisplayName("USE Entry는 음수 금액으로 생성됨")
        void createUse_shouldHaveNegativeAmount() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            String orderId = "ORDER-001";
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createUse(id, ledgerId, 500L, orderId, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.USE);
            assertThat(entry.amount()).isEqualTo(-500L);
            assertThat(entry.absoluteAmount()).isEqualTo(500L);
            assertThat(entry.orderId()).isEqualTo(orderId);
        }
    }

    @Nested
    @DisplayName("USE_CANCEL Entry 생성")
    class CreateUseCancelTest {

        @Test
        @DisplayName("USE_CANCEL Entry는 양수 금액으로 생성됨")
        void createUseCancel_shouldHavePositiveAmount() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            String orderId = "ORDER-001";
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createUseCancel(id, ledgerId, 300L, orderId, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.USE_CANCEL);
            assertThat(entry.amount()).isEqualTo(300L);
            assertThat(entry.orderId()).isEqualTo(orderId);
        }
    }

    @Nested
    @DisplayName("금액 헬퍼 메서드")
    class AmountHelperTest {

        @Test
        @DisplayName("absoluteAmount는 절대값 반환")
        void absoluteAmount_shouldReturnAbsoluteValue() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            LedgerEntry positiveEntry = LedgerEntry.createEarn(id, ledgerId, 1000L, now);
            LedgerEntry negativeEntry = LedgerEntry.createUse(UUID.randomUUID(), ledgerId, 500L, "ORDER", now);

            // THEN
            assertThat(positiveEntry.absoluteAmount()).isEqualTo(1000L);
            assertThat(negativeEntry.absoluteAmount()).isEqualTo(500L);
        }
    }
}
