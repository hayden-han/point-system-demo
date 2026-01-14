package com.musinsa.pointsystem.domain.model;


import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createEarn(id, 1000L, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.EARN);
            assertThat(entry.amount()).isEqualTo(1000L);
            assertThat(entry.isPositive()).isTrue();
            assertThat(entry.orderId()).isNull();
        }

        @Test
        @DisplayName("EARN Entry에 음수 금액 전달 시 예외")
        void createEarn_negativeAmount_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> new LedgerEntry(id, EntryType.EARN, -1000L, null, now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
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
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createEarnCancel(id, 1000L, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.EARN_CANCEL);
            assertThat(entry.amount()).isEqualTo(-1000L);
            assertThat(entry.isNegative()).isTrue();
            assertThat(entry.absoluteAmount()).isEqualTo(1000L);
            assertThat(entry.orderId()).isNull();
        }

        @Test
        @DisplayName("EARN_CANCEL Entry에 양수 금액 전달 시 예외")
        void createEarnCancel_positiveAmount_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> new LedgerEntry(id, EntryType.EARN_CANCEL, 1000L, null, now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
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
            String orderId = "ORDER-001";
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createUse(id, 500L, orderId, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.USE);
            assertThat(entry.amount()).isEqualTo(-500L);
            assertThat(entry.isNegative()).isTrue();
            assertThat(entry.absoluteAmount()).isEqualTo(500L);
            assertThat(entry.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("USE Entry에 양수 금액 전달 시 예외")
        void createUse_positiveAmount_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> new LedgerEntry(id, EntryType.USE, 500L, "ORDER-001", now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
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
            String orderId = "ORDER-001";
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            LedgerEntry entry = LedgerEntry.createUseCancel(id, 300L, orderId, now);

            // THEN
            assertThat(entry.type()).isEqualTo(EntryType.USE_CANCEL);
            assertThat(entry.amount()).isEqualTo(300L);
            assertThat(entry.isPositive()).isTrue();
            assertThat(entry.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("USE_CANCEL Entry에 음수 금액 전달 시 예외")
        void createUseCancel_negativeAmount_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> new LedgerEntry(id, EntryType.USE_CANCEL, -300L, "ORDER-001", now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }
    }

    @Nested
    @DisplayName("필수 값 검증")
    class ValidationTest {

        @Test
        @DisplayName("id가 null이면 예외")
        void nullId_shouldThrowException() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> LedgerEntry.createEarn(null, 1000L, now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("id");
        }

        @Test
        @DisplayName("type이 null이면 예외")
        void nullType_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN & THEN
            assertThatThrownBy(() -> new LedgerEntry(id, null, 1000L, null, now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("type");
        }

        @Test
        @DisplayName("createdAt이 null이면 예외")
        void nullCreatedAt_shouldThrowException() {
            // GIVEN
            UUID id = UUID.randomUUID();

            // WHEN & THEN
            assertThatThrownBy(() -> LedgerEntry.createEarn(id, 1000L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("createdAt");
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
            LocalDateTime now = LocalDateTime.now();
            LedgerEntry positiveEntry = LedgerEntry.createEarn(id, 1000L, now);
            LedgerEntry negativeEntry = LedgerEntry.createUse(UUID.randomUUID(), 500L, "ORDER", now);

            // THEN
            assertThat(positiveEntry.absoluteAmount()).isEqualTo(1000L);
            assertThat(negativeEntry.absoluteAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("isPositive는 양수 여부 반환")
        void isPositive_shouldReturnCorrectly() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            LedgerEntry earnEntry = LedgerEntry.createEarn(id, 1000L, now);
            LedgerEntry useEntry = LedgerEntry.createUse(UUID.randomUUID(), 500L, "ORDER", now);

            // THEN
            assertThat(earnEntry.isPositive()).isTrue();
            assertThat(useEntry.isPositive()).isFalse();
        }

        @Test
        @DisplayName("isNegative는 음수 여부 반환")
        void isNegative_shouldReturnCorrectly() {
            // GIVEN
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            LedgerEntry earnEntry = LedgerEntry.createEarn(id, 1000L, now);
            LedgerEntry useEntry = LedgerEntry.createUse(UUID.randomUUID(), 500L, "ORDER", now);

            // THEN
            assertThat(earnEntry.isNegative()).isFalse();
            assertThat(useEntry.isNegative()).isTrue();
        }
    }
}
