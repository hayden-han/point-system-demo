package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointLedgerTest {

    @Nested
    @DisplayName("적립건 생성")
    class CreateTest {

        @Test
        @DisplayName("기본 적립건 생성")
        void create_shouldInitializeCorrectly() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();

            // WHEN
            PointLedger ledger = PointLedgerFixture.create(id, memberId, 1000L, EarnType.SYSTEM);

            // THEN
            assertThat(ledger.memberId()).isEqualTo(memberId);
            assertThat(ledger.earnedAmount().getValue()).isEqualTo(1000L);
            assertThat(ledger.availableAmount().getValue()).isEqualTo(1000L);
            assertThat(ledger.usedAmount().getValue()).isEqualTo(0L);
            assertThat(ledger.canceled()).isFalse();
        }

        @Test
        @DisplayName("사용취소로 인한 신규 적립건 생성 (sourceTransactionId 포함)")
        void createWithSourceTransactionId_shouldSetSourceTransactionId() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            UUID sourceTransactionId = UuidGenerator.generate();

            // WHEN - 직접 생성자 호출로 sourceTransactionId 포함
            PointLedger ledger = new PointLedger(
                    id,
                    memberId,
                    PointAmount.of(500L),
                    PointAmount.of(500L),
                    PointAmount.ZERO,
                    EarnType.SYSTEM,
                    sourceTransactionId,
                    LocalDateTime.now().plusDays(365),
                    false,
                    LocalDateTime.now()
            );

            // THEN
            assertThat(ledger.sourceTransactionId()).isEqualTo(sourceTransactionId);
            assertThat(ledger.availableAmount().getValue()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("취소 가능 여부")
    class CanCancelTest {

        @Test
        @DisplayName("미사용 적립건은 취소 가능")
        void unusedLedger_canCancel() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            assertThat(ledger.canCancel()).isTrue();
        }

        @Test
        @DisplayName("일부 사용된 적립건은 취소 불가")
        void partiallyUsedLedger_cannotCancel() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(id, memberId, 1000L, 500L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }

        @Test
        @DisplayName("전액 사용된 적립건은 취소 불가")
        void fullyUsedLedger_cannotCancel() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createFullyUsed(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }

        @Test
        @DisplayName("이미 취소된 적립건은 취소 불가")
        void canceledLedger_cannotCancel() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createCanceled(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.canCancel()).isFalse();
        }
    }

    @Nested
    @DisplayName("적립건 취소")
    class CancelTest {

        @Test
        @DisplayName("미사용 적립건 취소 성공 - 새 객체 반환")
        void cancelUnused_shouldReturnNewCanceledLedger() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointLedger canceledLedger = ledger.cancel();

            // THEN
            assertThat(canceledLedger.canceled()).isTrue();
            assertThat(canceledLedger.availableAmount().getValue()).isEqualTo(0L);
            assertThat(ledger.canceled()).isFalse(); // 원본 불변
        }

        @Test
        @DisplayName("사용된 적립건 취소 시 예외 발생")
        void cancelUsed_shouldThrowException() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(id, memberId, 1000L, 500L, EarnType.SYSTEM);

            // WHEN & THEN
            assertThatThrownBy(ledger::cancel)
                    .isInstanceOf(PointLedgerAlreadyUsedException.class);
        }
    }

    @Nested
    @DisplayName("만료 여부")
    class ExpirationTest {

        @Test
        @DisplayName("만료일이 지나면 만료 상태")
        void pastExpiration_isExpired() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createExpired(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isExpired()).isTrue();
        }

        @Test
        @DisplayName("만료일이 지나지 않으면 유효 상태")
        void futureExpiration_isNotExpired() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            assertThat(ledger.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("사용 가능 여부")
    class AvailabilityTest {

        @Test
        @DisplayName("잔액이 있고 취소/만료되지 않으면 사용 가능")
        void validLedger_isAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            assertThat(ledger.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("취소된 적립건은 사용 불가")
        void canceledLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createCanceled(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("만료된 적립건은 사용 불가")
        void expiredLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createExpired(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("잔액이 0인 적립건은 사용 불가")
        void zeroBalanceLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createFullyUsed(id, memberId, 1000L, EarnType.SYSTEM);

            assertThat(ledger.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UseTest {

        @Test
        @DisplayName("요청 금액만큼 차감 - 새 객체 반환")
        void use_shouldReturnNewLedgerWithDeductedAmount() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointLedger.UseResult result = ledger.use(PointAmount.of(300L));

            // THEN
            assertThat(result.usedAmount().getValue()).isEqualTo(300L);
            assertThat(result.ledger().availableAmount().getValue()).isEqualTo(700L);
            assertThat(result.ledger().usedAmount().getValue()).isEqualTo(300L);
            assertThat(ledger.availableAmount().getValue()).isEqualTo(1000L); // 원본 불변
        }

        @Test
        @DisplayName("잔액보다 많이 요청하면 잔액만큼만 사용")
        void useMoreThanAvailable_shouldUseOnlyAvailable() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 500L);

            // WHEN
            PointLedger.UseResult result = ledger.use(PointAmount.of(800L));

            // THEN
            assertThat(result.usedAmount().getValue()).isEqualTo(500L);
            assertThat(result.ledger().availableAmount().getValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("포인트 복구")
    class RestoreTest {

        @Test
        @DisplayName("사용된 금액 내에서 복구 성공 - 새 객체 반환")
        void restore_shouldReturnNewLedgerWithRestoredAmount() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(id, memberId, 1000L, 500L, EarnType.SYSTEM);

            // WHEN
            PointLedger restoredLedger = ledger.restore(PointAmount.of(300L));

            // THEN
            assertThat(restoredLedger.availableAmount().getValue()).isEqualTo(800L);
            assertThat(restoredLedger.usedAmount().getValue()).isEqualTo(200L);
            assertThat(ledger.availableAmount().getValue()).isEqualTo(500L); // 원본 불변
        }

        @Test
        @DisplayName("사용된 금액보다 많이 복구하면 예외 발생")
        void restoreMoreThanUsed_shouldThrowException() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(id, memberId, 1000L, 500L, EarnType.SYSTEM);

            // WHEN & THEN
            assertThatThrownBy(() -> ledger.restore(PointAmount.of(600L)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("수기 지급 여부")
    class ManualTypeTest {

        @Test
        @DisplayName("MANUAL 타입이면 true")
        void manualType_isManual() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createManual(id, memberId, 1000L);

            assertThat(ledger.isManual()).isTrue();
        }

        @Test
        @DisplayName("SYSTEM 타입이면 false")
        void systemType_isNotManual() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            assertThat(ledger.isManual()).isFalse();
        }
    }
}
