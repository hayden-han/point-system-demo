package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.fixture.MemberPointFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberPointTest {

    private static final Long MAX_BALANCE = 10000000L;

    @Nested
    @DisplayName("MemberPoint 생성")
    class CreateTest {

        @Test
        @DisplayName("기본 생성 시 잔액이 0")
        void create_shouldHaveZeroBalance() {
            MemberPoint memberPoint = MemberPoint.create(1L);

            assertThat(memberPoint.getMemberId()).isEqualTo(1L);
            assertThat(memberPoint.getTotalBalance()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("잔액 증가")
    class IncreaseBalanceTest {

        @Test
        @DisplayName("잔액 증가 성공")
        void increaseBalance_shouldAddAmount() {
            // GIVEN
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 1000L);

            // WHEN
            memberPoint.increaseBalance(500L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("여러 번 증가 가능")
        void multipleIncrease_shouldAccumulate() {
            // GIVEN
            MemberPoint memberPoint = MemberPointFixture.create(1L);

            // WHEN
            memberPoint.increaseBalance(100L);
            memberPoint.increaseBalance(200L);
            memberPoint.increaseBalance(300L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(600L);
        }
    }

    @Nested
    @DisplayName("잔액 감소")
    class DecreaseBalanceTest {

        @Test
        @DisplayName("잔액 감소 성공")
        void decreaseBalance_shouldSubtractAmount() {
            // GIVEN
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 1000L);

            // WHEN
            memberPoint.decreaseBalance(300L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(700L);
        }

        @Test
        @DisplayName("잔액보다 많이 감소하면 예외 발생")
        void decreaseMoreThanBalance_shouldThrowException() {
            // GIVEN
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 500L);

            // WHEN & THEN
            assertThatThrownBy(() -> memberPoint.decreaseBalance(600L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("잔액이 부족");
        }

        @Test
        @DisplayName("정확히 잔액만큼 감소하면 0이 됨")
        void decreaseExactBalance_shouldBecomeZero() {
            // GIVEN
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 1000L);

            // WHEN
            memberPoint.decreaseBalance(1000L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("적립 가능 여부")
    class CanEarnTest {

        @Test
        @DisplayName("적립 후에도 최대 보유금액 이하면 true")
        void withinMaxBalance_canEarn() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 5000000L);

            assertThat(memberPoint.canEarn(4000000L, MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("적립 후 최대 보유금액 초과하면 false")
        void exceedsMaxBalance_cannotEarn() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 9500000L);

            assertThat(memberPoint.canEarn(600000L, MAX_BALANCE)).isFalse();
        }

        @Test
        @DisplayName("적립 후 정확히 최대 보유금액이면 true")
        void exactlyMaxBalance_canEarn() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 9000000L);

            assertThat(memberPoint.canEarn(1000000L, MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("이미 최대 보유금액이면 적립 불가")
        void alreadyMaxBalance_cannotEarn() {
            MemberPoint memberPoint = MemberPointFixture.createAtMaxBalance(1L, MAX_BALANCE);

            assertThat(memberPoint.canEarn(1L, MAX_BALANCE)).isFalse();
        }
    }

    @Nested
    @DisplayName("충분한 잔액 여부")
    class HasEnoughBalanceTest {

        @Test
        @DisplayName("잔액이 충분하면 true")
        void enoughBalance_returnsTrue() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 1000L);

            assertThat(memberPoint.hasEnoughBalance(500L)).isTrue();
        }

        @Test
        @DisplayName("잔액이 정확히 같으면 true")
        void exactBalance_returnsTrue() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 1000L);

            assertThat(memberPoint.hasEnoughBalance(1000L)).isTrue();
        }

        @Test
        @DisplayName("잔액이 부족하면 false")
        void insufficientBalance_returnsFalse() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 500L);

            assertThat(memberPoint.hasEnoughBalance(1000L)).isFalse();
        }

        @Test
        @DisplayName("잔액이 0이면 false")
        void zeroBalance_returnsFalse() {
            MemberPoint memberPoint = MemberPointFixture.create(1L);

            assertThat(memberPoint.hasEnoughBalance(1L)).isFalse();
        }
    }
}
