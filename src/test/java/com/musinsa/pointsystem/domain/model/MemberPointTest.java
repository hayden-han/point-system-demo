package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.fixture.MemberPointFixture;
import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

            assertThat(memberPoint.getMemberId()).isEqualTo(memberId);
            assertThat(memberPoint.getTotalBalance().getValue()).isEqualTo(0L);
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
            assertThat(result.memberPoint().getTotalBalance().getValue()).isEqualTo(700L);
            assertThat(result.usageDetails()).hasSize(1);
            assertThat(memberPoint.getTotalBalance().getValue()).isEqualTo(1000L); // 원본 불변
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
            assertThat(found.getId()).isEqualTo(ledgerId);
        }
    }
}
