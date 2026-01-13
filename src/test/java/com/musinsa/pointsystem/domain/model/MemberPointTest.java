package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
import com.musinsa.pointsystem.domain.port.IdGenerator;
import com.musinsa.pointsystem.fixture.MemberPointFixture;
import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberPointTest {

    private static final PointAmount MAX_BALANCE = PointAmount.of(10000000L);

    @Nested
    @DisplayName("MemberPoint 생성")
    class CreateTest {

        @Test
        @DisplayName("기본 생성 시 잔액이 0")
        void create_shouldHaveZeroBalance() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPoint.create(memberId);

            assertThat(memberPoint.memberId()).isEqualTo(memberId);
            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UseTest {

        @Test
        @DisplayName("사용 성공 시 새 MemberPoint 반환")
        void use_shouldReturnNewMemberPoint() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(UuidGenerator.generate(), memberId, 1000L);
            MemberPoint memberPoint = MemberPointFixture.createWithLedgers(memberId, 1000L, List.of(ledger));

            // WHEN
            MemberPoint.UsageResult result = memberPoint.use(PointAmount.of(300L));

            // THEN
            assertThat(result.memberPoint().totalBalance().getValue()).isEqualTo(700L);
            assertThat(result.usageDetails()).hasSize(1);
            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(1000L); // 원본 불변
        }

        @Test
        @DisplayName("잔액보다 많이 사용하면 예외 발생")
        void useMoreThanBalance_shouldThrowException() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(UuidGenerator.generate(), memberId, 500L);
            MemberPoint memberPoint = MemberPointFixture.createWithLedgers(memberId, 500L, List.of(ledger));

            // WHEN & THEN
            assertThatThrownBy(() -> memberPoint.use(PointAmount.of(600L)))
                    .isInstanceOf(InsufficientPointException.class);
        }
    }

    @Nested
    @DisplayName("적립 가능 여부")
    class CanEarnTest {

        @Test
        @DisplayName("적립 후에도 최대 보유금액 이하면 true")
        void withinMaxBalance_canEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 5000000L);

            assertThat(memberPoint.canEarn(PointAmount.of(4000000L), MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("적립 후 최대 보유금액 초과하면 false")
        void exceedsMaxBalance_cannotEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9500000L);

            assertThat(memberPoint.canEarn(PointAmount.of(600000L), MAX_BALANCE)).isFalse();
        }

        @Test
        @DisplayName("적립 후 정확히 최대 보유금액이면 true")
        void exactlyMaxBalance_canEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9000000L);

            assertThat(memberPoint.canEarn(PointAmount.of(1000000L), MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("이미 최대 보유금액이면 적립 불가")
        void alreadyMaxBalance_cannotEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createAtMaxBalance(memberId, MAX_BALANCE.getValue());

            assertThat(memberPoint.canEarn(PointAmount.of(1L), MAX_BALANCE)).isFalse();
        }
    }

    @Nested
    @DisplayName("충분한 잔액 여부")
    class HasEnoughBalanceTest {

        @Test
        @DisplayName("잔액이 충분하면 true")
        void enoughBalance_returnsTrue() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

            assertThat(memberPoint.hasEnoughBalance(PointAmount.of(500L))).isTrue();
        }

        @Test
        @DisplayName("잔액이 정확히 같으면 true")
        void exactBalance_returnsTrue() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

            assertThat(memberPoint.hasEnoughBalance(PointAmount.of(1000L))).isTrue();
        }

        @Test
        @DisplayName("잔액이 부족하면 false")
        void insufficientBalance_returnsFalse() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 500L);

            assertThat(memberPoint.hasEnoughBalance(PointAmount.of(1000L))).isFalse();
        }

        @Test
        @DisplayName("잔액이 0이면 false")
        void zeroBalance_returnsFalse() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.create(memberId);

            assertThat(memberPoint.hasEnoughBalance(PointAmount.of(1L))).isFalse();
        }
    }

    @Nested
    @DisplayName("적립건 조회")
    class FindLedgerTest {

        @Test
        @DisplayName("적립건 ID로 조회 성공")
        void findLedgerById_shouldReturnLedger() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            UUID ledgerId = UuidGenerator.generate();
            PointLedger ledger = PointLedgerFixture.createSystem(ledgerId, memberId, 1000L);
            MemberPoint memberPoint = MemberPointFixture.createWithLedgers(memberId, 1000L, List.of(ledger));

            // WHEN
            PointLedger found = memberPoint.findLedgerById(ledgerId);

            // THEN
            assertThat(found.id()).isEqualTo(ledgerId);
        }
    }

    // =====================================================
    // v2 구조 테스트 (LedgerEntry 기반)
    // =====================================================

    @Nested
    @DisplayName("v2: getTotalBalance")
    class GetTotalBalanceTest {

        @Test
        @DisplayName("사용 가능한 Ledger의 availableAmount 합계 반환")
        void getTotalBalance_shouldSumAvailableAmounts() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger ledger1 = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger ledger2 = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.MANUAL,
                    now.plusDays(30), null, idGenerator, now
            );

            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger1, ledger2));

            // WHEN
            PointAmount totalBalance = memberPoint.getTotalBalance(now);

            // THEN
            assertThat(totalBalance).isEqualTo(PointAmount.of(1500L));
        }

        @Test
        @DisplayName("만료된 Ledger는 잔액에서 제외")
        void getTotalBalance_shouldExcludeExpiredLedgers() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime past = LocalDateTime.now().minusDays(10);
            LocalDateTime now = LocalDateTime.now();

            PointLedger validLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger expiredLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    past.plusDays(5), null, idGenerator, past // 이미 만료
            );

            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(validLedger, expiredLedger));

            // WHEN
            PointAmount totalBalance = memberPoint.getTotalBalance(now);

            // THEN
            assertThat(totalBalance).isEqualTo(PointAmount.of(1000L));
        }

        @Test
        @DisplayName("취소된 Ledger는 잔액에서 제외")
        void getTotalBalance_shouldExcludeCanceledLedgers() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger validLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger canceledLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            ).cancel(idGenerator, now).ledger();

            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(validLedger, canceledLedger));

            // WHEN
            PointAmount totalBalance = memberPoint.getTotalBalance(now);

            // THEN
            assertThat(totalBalance).isEqualTo(PointAmount.of(1000L));
        }

        @Test
        @DisplayName("잔액이 0인 Ledger는 잔액에서 제외")
        void getTotalBalance_shouldExcludeZeroBalanceLedgers() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger validLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger fullyUsedLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            ).use(PointAmount.of(500L), "ORDER-001", idGenerator, now).ledger();

            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(validLedger, fullyUsedLedger));

            // WHEN
            PointAmount totalBalance = memberPoint.getTotalBalance(now);

            // THEN
            assertThat(totalBalance).isEqualTo(PointAmount.of(1000L));
        }
    }

    @Nested
    @DisplayName("v2: 포인트 사용 with orderId")
    class UseWithOrderIdTest {

        @Test
        @DisplayName("사용 성공 시 USE Entry 추가")
        void use_shouldAddUseEntry() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            String orderId = "ORDER-001";

            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger));

            // WHEN
            MemberPoint.UsageResult result = memberPoint.use(PointAmount.of(300L), orderId, idGenerator, now);

            // THEN
            assertThat(result.memberPoint().getTotalBalance(now)).isEqualTo(PointAmount.of(700L));
            assertThat(result.usageDetails()).hasSize(1);
            assertThat(result.usageDetails().get(0).usedAmount()).isEqualTo(PointAmount.of(300L));
        }

        @Test
        @DisplayName("여러 Ledger에서 차감 - 수기지급 우선")
        void use_shouldDeductFromManualFirst() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger systemLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            PointLedger manualLedger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(300L), EarnType.MANUAL,
                    now.plusDays(30), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(systemLedger, manualLedger));

            // WHEN
            MemberPoint.UsageResult result = memberPoint.use(PointAmount.of(400L), "ORDER-001", idGenerator, now);

            // THEN
            assertThat(result.memberPoint().getTotalBalance(now)).isEqualTo(PointAmount.of(400L));
            assertThat(result.usageDetails()).hasSize(2);
            // MANUAL 먼저 사용 (300) + SYSTEM에서 100
            assertThat(result.usageDetails().get(0).usedAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(result.usageDetails().get(1).usedAmount()).isEqualTo(PointAmount.of(100L));
        }

        @Test
        @DisplayName("잔액 부족 시 예외")
        void use_insufficientBalance_shouldThrowException() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger));

            // WHEN & THEN
            assertThatThrownBy(() -> memberPoint.use(PointAmount.of(1000L), "ORDER-001", idGenerator, now))
                    .isInstanceOf(InsufficientPointException.class);
        }
    }

    @Nested
    @DisplayName("v2: 사용 취소")
    class CancelUseTest {

        @Test
        @DisplayName("미만료 Ledger 취소 - 잔액 복구")
        void cancelUse_notExpired_shouldRestoreBalance() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            String orderId = "ORDER-001";

            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger));
            MemberPoint afterUse = memberPoint.use(PointAmount.of(500L), orderId, idGenerator, now).memberPoint();

            // WHEN
            MemberPoint.CancelUseResult result = afterUse.cancelUse(orderId, PointAmount.of(300L), now, idGenerator, 365);

            // THEN
            assertThat(result.memberPoint().getTotalBalance(now)).isEqualTo(PointAmount.of(800L));
            assertThat(result.restoredLedgers()).hasSize(1);
            assertThat(result.newLedgers()).isEmpty();
        }

        @Test
        @DisplayName("만료된 Ledger 취소 - 신규 Ledger 생성")
        void cancelUse_expired_shouldCreateNewLedger() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime past = LocalDateTime.now().minusDays(10);
            LocalDateTime now = LocalDateTime.now();
            String orderId = "ORDER-001";

            // 과거에 생성된 만료될 Ledger
            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    past.plusDays(5), null, idGenerator, past // 5일 후 만료
            );
            PointLedger usedLedger = ledger.use(PointAmount.of(500L), orderId, idGenerator, past).ledger();
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(usedLedger));

            // WHEN - now 시점에서 취소 (ledger는 이미 만료됨)
            MemberPoint.CancelUseResult result = memberPoint.cancelUse(orderId, PointAmount.of(300L), now, idGenerator, 365);

            // THEN
            assertThat(result.memberPoint().getTotalBalance(now)).isEqualTo(PointAmount.of(300L));
            assertThat(result.restoredLedgers()).isEmpty(); // 만료된 건이라 restored 아님
            assertThat(result.newLedgers()).hasSize(1);
            assertThat(result.newLedgers().get(0).availableAmount()).isEqualTo(PointAmount.of(300L));
        }

        @Test
        @DisplayName("취소 가능 금액 초과 시 예외")
        void cancelUse_exceedCancelable_shouldThrowException() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            String orderId = "ORDER-001";

            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger));
            MemberPoint afterUse = memberPoint.use(PointAmount.of(300L), orderId, idGenerator, now).memberPoint();

            // WHEN & THEN - 300원 사용했는데 500원 취소
            assertThatThrownBy(() -> afterUse.cancelUse(orderId, PointAmount.of(500L), now, idGenerator, 365))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 예외")
        void cancelUse_nonExistentOrder_shouldThrowException() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();

            PointLedger ledger = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(1000L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger));

            // WHEN & THEN
            assertThatThrownBy(() -> memberPoint.cancelUse("NON-EXISTENT", PointAmount.of(100L), now, idGenerator, 365))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }

        @Test
        @DisplayName("여러 Ledger에서 취소")
        void cancelUse_multipleLedgers() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            IdGenerator idGenerator = UuidGenerator::generate;
            LocalDateTime now = LocalDateTime.now();
            String orderId = "ORDER-001";

            PointLedger ledger1 = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.MANUAL,
                    now.plusDays(30), null, idGenerator, now
            );
            PointLedger ledger2 = PointLedger.create(
                    UuidGenerator.generate(), memberId, PointAmount.of(500L), EarnType.SYSTEM,
                    now.plusDays(365), null, idGenerator, now
            );
            MemberPoint memberPoint = MemberPoint.of(memberId, List.of(ledger1, ledger2));

            // 700원 사용 (MANUAL 500 + SYSTEM 200)
            MemberPoint afterUse = memberPoint.use(PointAmount.of(700L), orderId, idGenerator, now).memberPoint();
            assertThat(afterUse.getTotalBalance(now)).isEqualTo(PointAmount.of(300L));

            // WHEN - 500원 취소
            MemberPoint.CancelUseResult result = afterUse.cancelUse(orderId, PointAmount.of(500L), now, idGenerator, 365);

            // THEN
            assertThat(result.memberPoint().getTotalBalance(now)).isEqualTo(PointAmount.of(800L));
        }
    }
}
