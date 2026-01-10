package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointLedgerTest {

    @Nested
    @DisplayName("적립건 생성")
    class CreateTest {

        @Test
        @DisplayName("기본 적립건 생성")
        void create_shouldInitializeCorrectly() {
            // WHEN
            PointLedger ledger = PointLedger.create(1L, 1000L, EarnType.SYSTEM, LocalDateTime.now().plusDays(365));

            // THEN
            assertThat(ledger.getMemberId()).isEqualTo(1L);
            assertThat(ledger.getEarnedAmount()).isEqualTo(1000L);
            assertThat(ledger.getAvailableAmount()).isEqualTo(1000L);
            assertThat(ledger.getUsedAmount()).isEqualTo(0L);
            assertThat(ledger.isCanceled()).isFalse();
        }

        @Test
        @DisplayName("사용취소로 인한 신규 적립건 생성")
        void createFromCancelUse_shouldSetSourceTransactionId() {
            // WHEN
            PointLedger ledger = PointLedger.createFromCancelUse(
                    1L, 500L, EarnType.SYSTEM, LocalDateTime.now().plusDays(365), 100L);

            // THEN
            assertThat(ledger.getSourceTransactionId()).isEqualTo(100L);
            assertThat(ledger.getAvailableAmount()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("취소 가능 여부")
    class CanCancelTest {

        @Test
        @DisplayName("미사용 적립건은 취소 가능")
        void unusedLedger_canCancel() {
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            assertThat(ledger.canCancel()).isTrue();
        }

        @Test
        @DisplayName("일부 사용된 적립건은 취소 불가")
        void partiallyUsedLedger_cannotCancel() {
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(1L, 1L, 1000L, 500L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }

        @Test
        @DisplayName("전액 사용된 적립건은 취소 불가")
        void fullyUsedLedger_cannotCancel() {
            PointLedger ledger = PointLedgerFixture.createFullyUsed(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }

        @Test
        @DisplayName("이미 취소된 적립건은 취소 불가")
        void canceledLedger_cannotCancel() {
            PointLedger ledger = PointLedgerFixture.createCanceled(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }
    }

    @Nested
    @DisplayName("적립건 취소")
    class CancelTest {

        @Test
        @DisplayName("미사용 적립건 취소 성공")
        void cancelUnused_shouldSucceed() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            // WHEN
            ledger.cancel();

            // THEN
            assertThat(ledger.isCanceled()).isTrue();
            assertThat(ledger.getAvailableAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("사용된 적립건 취소 시 예외 발생")
        void cancelUsed_shouldThrowException() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(1L, 1L, 1000L, 500L, EarnType.SYSTEM);

            // WHEN & THEN
            assertThatThrownBy(() -> ledger.cancel())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("만료 여부")
    class ExpirationTest {

        @Test
        @DisplayName("만료일이 지나면 만료 상태")
        void pastExpiration_isExpired() {
            PointLedger ledger = PointLedgerFixture.createExpired(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isExpired()).isTrue();
        }

        @Test
        @DisplayName("만료일이 지나지 않으면 유효 상태")
        void futureExpiration_isNotExpired() {
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            assertThat(ledger.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("사용 가능 여부")
    class AvailabilityTest {

        @Test
        @DisplayName("잔액이 있고 취소/만료되지 않으면 사용 가능")
        void validLedger_isAvailable() {
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            assertThat(ledger.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("취소된 적립건은 사용 불가")
        void canceledLedger_isNotAvailable() {
            PointLedger ledger = PointLedgerFixture.createCanceled(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("만료된 적립건은 사용 불가")
        void expiredLedger_isNotAvailable() {
            PointLedger ledger = PointLedgerFixture.createExpired(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("잔액이 0인 적립건은 사용 불가")
        void zeroBalanceLedger_isNotAvailable() {
            PointLedger ledger = PointLedgerFixture.createFullyUsed(1L, 1L, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UseTest {

        @Test
        @DisplayName("요청 금액만큼 차감")
        void use_shouldDeductAmount() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            // WHEN
            Long usedAmount = ledger.use(300L);

            // THEN
            assertThat(usedAmount).isEqualTo(300L);
            assertThat(ledger.getAvailableAmount()).isEqualTo(700L);
            assertThat(ledger.getUsedAmount()).isEqualTo(300L);
        }

        @Test
        @DisplayName("잔액보다 많이 요청하면 잔액만큼만 사용")
        void useMoreThanAvailable_shouldUseOnlyAvailable() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 500L);

            // WHEN
            Long usedAmount = ledger.use(800L);

            // THEN
            assertThat(usedAmount).isEqualTo(500L);
            assertThat(ledger.getAvailableAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("포인트 복구")
    class RestoreTest {

        @Test
        @DisplayName("사용된 금액 내에서 복구 성공")
        void restore_shouldIncreaseAvailableAmount() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(1L, 1L, 1000L, 500L, EarnType.SYSTEM);

            // WHEN
            ledger.restore(300L);

            // THEN
            assertThat(ledger.getAvailableAmount()).isEqualTo(800L);
            assertThat(ledger.getUsedAmount()).isEqualTo(200L);
        }

        @Test
        @DisplayName("사용된 금액보다 많이 복구하면 예외 발생")
        void restoreMoreThanUsed_shouldThrowException() {
            // GIVEN
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(1L, 1L, 1000L, 500L, EarnType.SYSTEM);

            // WHEN & THEN
            assertThatThrownBy(() -> ledger.restore(600L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("수기 지급 여부")
    class ManualTypeTest {

        @Test
        @DisplayName("MANUAL 타입이면 true")
        void manualType_isManual() {
            PointLedger ledger = PointLedgerFixture.createManual(1L, 1L, 1000L);

            assertThat(ledger.isManual()).isTrue();
        }

        @Test
        @DisplayName("SYSTEM 타입이면 false")
        void systemType_isNotManual() {
            PointLedger ledger = PointLedgerFixture.createSystem(1L, 1L, 1000L);

            assertThat(ledger.isManual()).isFalse();
        }
    }
}
