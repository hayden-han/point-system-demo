package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.domain.port.IdGenerator;
import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
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
        @DisplayName("사용취소로 인한 신규 적립건 생성 (sourceLedgerId 포함)")
        void createWithSourceLedgerId_shouldSetSourceLedgerId() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            UUID sourceLedgerId = UuidGenerator.generate();

            // WHEN - 직접 생성자 호출로 sourceLedgerId 포함
            PointLedger ledger = new PointLedger(
                    id,
                    memberId,
                    PointAmount.of(500L),
                    PointAmount.of(500L),
                    EarnType.SYSTEM,
                    sourceLedgerId,
                    LocalDateTime.now().plusDays(365),
                    false,
                    LocalDateTime.now(),
                    List.of()
            );

            // THEN
            assertThat(ledger.sourceLedgerId()).isEqualTo(sourceLedgerId);
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
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointLedger.CancelResult cancelResult = ledger.cancel(idGenerator, now);

            // THEN
            assertThat(cancelResult.ledger().canceled()).isTrue();
            assertThat(cancelResult.ledger().availableAmount().getValue()).isEqualTo(0L);
            assertThat(ledger.canceled()).isFalse(); // 원본 불변
        }

        @Test
        @DisplayName("사용된 적립건 취소 시 예외 발생")
        void cancelUsed_shouldThrowException() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedgerFixture.createPartiallyUsed(id, memberId, 1000L, 500L, EarnType.SYSTEM);

            // WHEN & THEN
            assertThatThrownBy(() -> ledger.cancel(idGenerator, now))
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
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isExpired(now)).isTrue();
        }

        @Test
        @DisplayName("만료일이 지나지 않으면 유효 상태")
        void futureExpiration_isNotExpired() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isExpired(now)).isFalse();
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
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isAvailable(now)).isTrue();
        }

        @Test
        @DisplayName("취소된 적립건은 사용 불가")
        void canceledLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createCanceled(id, memberId, 1000L, EarnType.SYSTEM);
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isAvailable(now)).isFalse();
        }

        @Test
        @DisplayName("만료된 적립건은 사용 불가")
        void expiredLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createExpired(id, memberId, 1000L, EarnType.SYSTEM);
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isAvailable(now)).isFalse();
        }

        @Test
        @DisplayName("잔액이 0인 적립건은 사용 불가")
        void zeroBalanceLedger_isNotAvailable() {
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createFullyUsed(id, memberId, 1000L, EarnType.SYSTEM);
            LocalDateTime now = LocalDateTime.now();

            assertThat(ledger.isAvailable(now)).isFalse();
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
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 1000L);

            // WHEN
            PointLedger.UseResult result = ledger.use(PointAmount.of(300L), "ORDER-001", idGenerator, now);

            // THEN
            assertThat(result.usedAmount().getValue()).isEqualTo(300L);
            assertThat(result.ledger().availableAmount().getValue()).isEqualTo(700L);
            assertThat(ledger.availableAmount().getValue()).isEqualTo(1000L); // 원본 불변
        }

        @Test
        @DisplayName("잔액보다 많이 요청하면 잔액만큼만 사용")
        void useMoreThanAvailable_shouldUseOnlyAvailable() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedgerFixture.createSystem(id, memberId, 500L);

            // WHEN
            PointLedger.UseResult result = ledger.use(PointAmount.of(800L), "ORDER-001", idGenerator, now);

            // THEN
            assertThat(result.usedAmount().getValue()).isEqualTo(500L);
            assertThat(result.ledger().availableAmount().getValue()).isEqualTo(0L);
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

    // =====================================================
    // v2 구조 테스트 (LedgerEntry 기반)
    // =====================================================

    @Nested
    @DisplayName("v2: 적립건 생성 with Entry")
    class CreateWithEntryTest {

        @Test
        @DisplayName("create() 시 EARN Entry가 자동 생성됨")
        void create_shouldHaveEarnEntry() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );

            // THEN
            assertThat(ledger.entries()).hasSize(1);
            assertThat(ledger.entries().get(0).type()).isEqualTo(EntryType.EARN);
            assertThat(ledger.entries().get(0).amount()).isEqualTo(1000L);
            assertThat(ledger.entries().get(0).orderId()).isNull();
        }
    }

    @Nested
    @DisplayName("v2: 포인트 사용 with Entry")
    class UseWithEntryTest {

        @Test
        @DisplayName("use(orderId) 시 USE Entry가 추가됨")
        void useWithOrderId_shouldAddUseEntry() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            String orderId = "ORDER-001";

            // WHEN
            PointLedger.UseResult result = ledger.use(PointAmount.of(300L), orderId, idGenerator, now);

            // THEN
            assertThat(result.usedAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(result.ledger().availableAmount()).isEqualTo(PointAmount.of(700L));
            assertThat(result.ledger().entries()).hasSize(2);

            LedgerEntry useEntry = result.ledger().entries().get(1);
            assertThat(useEntry.type()).isEqualTo(EntryType.USE);
            assertThat(useEntry.amount()).isEqualTo(-300L);
            assertThat(useEntry.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("여러 번 사용 시 각각 USE Entry 추가")
        void multipleUse_shouldAddMultipleEntries() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );

            // WHEN
            PointLedger.UseResult result1 = ledger.use(PointAmount.of(300L), "ORDER-001", idGenerator, now);
            PointLedger.UseResult result2 = result1.ledger().use(PointAmount.of(200L), "ORDER-002", idGenerator, now);

            // THEN
            assertThat(result2.ledger().entries()).hasSize(3); // EARN + USE + USE
            assertThat(result2.ledger().availableAmount()).isEqualTo(PointAmount.of(500L));
            assertThat(result2.ledger().usedAmount()).isEqualTo(PointAmount.of(500L));
        }
    }

    @Nested
    @DisplayName("v2: 적립 취소 with Entry")
    class CancelWithEntryTest {

        @Test
        @DisplayName("cancel(idGenerator) 시 EARN_CANCEL Entry 추가")
        void cancelWithIdGenerator_shouldAddEarnCancelEntry() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );

            // WHEN
            PointLedger.CancelResult result = ledger.cancel(idGenerator, now);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(PointAmount.of(1000L));
            assertThat(result.ledger().canceled()).isTrue();
            assertThat(result.ledger().availableAmount()).isEqualTo(PointAmount.ZERO);
            assertThat(result.ledger().entries()).hasSize(2);

            LedgerEntry cancelEntry = result.ledger().entries().get(1);
            assertThat(cancelEntry.type()).isEqualTo(EntryType.EARN_CANCEL);
            assertThat(cancelEntry.amount()).isEqualTo(-1000L);
        }

        @Test
        @DisplayName("이미 취소된 적립건 재취소 시 예외")
        void cancelAlreadyCanceled_shouldThrowException() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger canceledLedger = ledger.cancel(idGenerator, now).ledger();

            // WHEN & THEN
            assertThatThrownBy(() -> canceledLedger.cancel(idGenerator, now))
                    .isInstanceOf(PointLedgerAlreadyCanceledException.class);
        }

        @Test
        @DisplayName("사용된 적립건 취소 시 예외")
        void cancelUsedLedger_shouldThrowException() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger usedLedger = ledger.use(PointAmount.of(100L), "ORDER-001", idGenerator, now).ledger();

            // WHEN & THEN
            assertThatThrownBy(() -> usedLedger.cancel(idGenerator, now))
                    .isInstanceOf(PointLedgerAlreadyUsedException.class);
        }
    }

    @Nested
    @DisplayName("v2: 주문별 취소 가능 금액")
    class CancelableAmountByOrderTest {

        @Test
        @DisplayName("사용된 금액만큼 취소 가능")
        void getCancelableAmount_shouldReturnUsedAmount() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            String orderId = "ORDER-001";
            PointLedger usedLedger = ledger.use(PointAmount.of(500L), orderId, idGenerator, now).ledger();

            // WHEN
            PointAmount cancelable = usedLedger.getCancelableAmountByOrder(orderId);

            // THEN
            assertThat(cancelable).isEqualTo(PointAmount.of(500L));
        }

        @Test
        @DisplayName("다른 주문의 사용은 취소 가능 금액에 포함 안됨")
        void getCancelableAmount_shouldOnlyCountSameOrder() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger afterUse1 = ledger.use(PointAmount.of(300L), "ORDER-001", idGenerator, now).ledger();
            PointLedger afterUse2 = afterUse1.use(PointAmount.of(200L), "ORDER-002", idGenerator, now).ledger();

            // WHEN
            PointAmount cancelableOrder1 = afterUse2.getCancelableAmountByOrder("ORDER-001");
            PointAmount cancelableOrder2 = afterUse2.getCancelableAmountByOrder("ORDER-002");

            // THEN
            assertThat(cancelableOrder1).isEqualTo(PointAmount.of(300L));
            assertThat(cancelableOrder2).isEqualTo(PointAmount.of(200L));
        }

        @Test
        @DisplayName("부분 취소 후 남은 금액만 취소 가능")
        void getCancelableAmount_afterPartialCancel() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            String orderId = "ORDER-001";
            PointLedger afterUse = ledger.use(PointAmount.of(500L), orderId, idGenerator, now).ledger();
            PointLedger afterCancel = afterUse.cancelUse(orderId, PointAmount.of(200L), now, idGenerator, 365).ledger();

            // WHEN
            PointAmount cancelable = afterCancel.getCancelableAmountByOrder(orderId);

            // THEN
            assertThat(cancelable).isEqualTo(PointAmount.of(300L));
        }

        @Test
        @DisplayName("존재하지 않는 주문은 취소 가능 금액 0")
        void getCancelableAmount_nonExistentOrder() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );

            // WHEN
            PointAmount cancelable = ledger.getCancelableAmountByOrder("NON-EXISTENT");

            // THEN
            assertThat(cancelable).isEqualTo(PointAmount.ZERO);
        }
    }

    @Nested
    @DisplayName("v2: 사용 취소")
    class CancelUseTest {

        @Test
        @DisplayName("미만료 적립건 사용취소 - 잔액 복구")
        void cancelUse_notExpired_shouldRestoreBalance() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            String orderId = "ORDER-001";
            PointLedger afterUse = ledger.use(PointAmount.of(500L), orderId, idGenerator, now).ledger();

            // WHEN
            PointLedger.CancelUseResult result = afterUse.cancelUse(orderId, PointAmount.of(300L), now, idGenerator, 365);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(result.ledger().availableAmount()).isEqualTo(PointAmount.of(800L)); // 500 + 300
            assertThat(result.newLedger()).isNull(); // 미만료이므로 신규 적립건 없음
            assertThat(result.ledger().entries()).hasSize(3); // EARN + USE + USE_CANCEL
        }

        @Test
        @DisplayName("만료된 적립건 사용취소 - 신규 적립건 생성")
        void cancelUse_expired_shouldCreateNewLedger() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime past = LocalDateTime.now().minusDays(10);
            LocalDateTime now = LocalDateTime.now();
            // 과거에 생성되고 이미 만료된 적립건
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    past.plusDays(5), null, idGenerator, past // 만료일이 5일 전
            );
            String orderId = "ORDER-001";
            PointLedger afterUse = ledger.use(PointAmount.of(500L), orderId, idGenerator, past).ledger();

            // WHEN
            PointLedger.CancelUseResult result = afterUse.cancelUse(orderId, PointAmount.of(300L), now, idGenerator, 365);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(result.ledger().availableAmount()).isEqualTo(PointAmount.of(500L)); // 복구되지 않음
            assertThat(result.newLedger()).isNotNull();
            assertThat(result.newLedger().availableAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(result.newLedger().sourceLedgerId()).isEqualTo(id);
        }

        @Test
        @DisplayName("취소 가능 금액 초과 요청 - 가능한 만큼만 취소")
        void cancelUse_exceedCancelable_shouldCancelOnlyAvailable() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            String orderId = "ORDER-001";
            PointLedger afterUse = ledger.use(PointAmount.of(300L), orderId, idGenerator, now).ledger();

            // WHEN - 300원 사용했는데 500원 취소 요청
            PointLedger.CancelUseResult result = afterUse.cancelUse(orderId, PointAmount.of(500L), now, idGenerator, 365);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(PointAmount.of(300L)); // 가능한 만큼만
            assertThat(result.ledger().availableAmount()).isEqualTo(PointAmount.of(1000L)); // 완전 복구
        }

        @Test
        @DisplayName("취소할 것이 없으면 0원 취소")
        void cancelUse_nothingToCancel_shouldReturnZero() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );

            // WHEN - 사용한 적 없는 주문 취소 시도
            PointLedger.CancelUseResult result = ledger.cancelUse("ORDER-001", PointAmount.of(500L), now, idGenerator, 365);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(PointAmount.ZERO);
            assertThat(result.ledger()).isSameAs(ledger); // 변경 없이 동일 객체
            assertThat(result.newLedger()).isNull();
        }
    }

    @Nested
    @DisplayName("v2: usedAmount 계산")
    class UsedAmountCalculationTest {

        @Test
        @DisplayName("사용된 금액은 entries 기반으로 계산")
        void usedAmount_shouldCalculateFromEntries() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger afterUse = ledger.use(PointAmount.of(500L), "ORDER-001", idGenerator, now).ledger();

            // WHEN
            PointAmount usedAmount = afterUse.usedAmount();

            // THEN
            assertThat(usedAmount).isEqualTo(PointAmount.of(500L));
        }

        @Test
        @DisplayName("사용 취소 후 usedAmount 감소")
        void usedAmount_afterCancel_shouldDecrease() {
            // GIVEN
            UUID id = UuidGenerator.generate();
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            PointLedger ledger = PointLedger.create(
                    id, memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger afterUse = ledger.use(PointAmount.of(500L), "ORDER-001", idGenerator, now).ledger();
            PointLedger afterCancel = afterUse.cancelUse("ORDER-001", PointAmount.of(200L), now, idGenerator, 365).ledger();

            // WHEN
            PointAmount usedAmount = afterCancel.usedAmount();

            // THEN
            assertThat(usedAmount).isEqualTo(PointAmount.of(300L)); // 500 - 200
        }
    }
}
