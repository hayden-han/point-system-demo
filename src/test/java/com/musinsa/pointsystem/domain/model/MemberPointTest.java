package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.fixture.MemberPointFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPoint.create(memberId);

            assertThat(memberPoint.getMemberId()).isEqualTo(memberId);
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
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

            // WHEN
            memberPoint.increaseBalance(500L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("여러 번 증가 가능")
        void multipleIncrease_shouldAccumulate() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.create(memberId);

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
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

            // WHEN
            memberPoint.decreaseBalance(300L);

            // THEN
            assertThat(memberPoint.getTotalBalance()).isEqualTo(700L);
        }

        @Test
        @DisplayName("잔액보다 많이 감소하면 예외 발생")
        void decreaseMoreThanBalance_shouldThrowException() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 500L);

            // WHEN & THEN
            assertThatThrownBy(() -> memberPoint.decreaseBalance(600L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("잔액이 부족");
        }

        @Test
        @DisplayName("정확히 잔액만큼 감소하면 0이 됨")
        void decreaseExactBalance_shouldBecomeZero() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

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
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 5000000L);

            assertThat(memberPoint.canEarn(4000000L, MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("적립 후 최대 보유금액 초과하면 false")
        void exceedsMaxBalance_cannotEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9500000L);

            assertThat(memberPoint.canEarn(600000L, MAX_BALANCE)).isFalse();
        }

        @Test
        @DisplayName("적립 후 정확히 최대 보유금액이면 true")
        void exactlyMaxBalance_canEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9000000L);

            assertThat(memberPoint.canEarn(1000000L, MAX_BALANCE)).isTrue();
        }

        @Test
        @DisplayName("이미 최대 보유금액이면 적립 불가")
        void alreadyMaxBalance_cannotEarn() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createAtMaxBalance(memberId, MAX_BALANCE);

            assertThat(memberPoint.canEarn(1L, MAX_BALANCE)).isFalse();
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

            assertThat(memberPoint.hasEnoughBalance(500L)).isTrue();
        }

        @Test
        @DisplayName("잔액이 정확히 같으면 true")
        void exactBalance_returnsTrue() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 1000L);

            assertThat(memberPoint.hasEnoughBalance(1000L)).isTrue();
        }

        @Test
        @DisplayName("잔액이 부족하면 false")
        void insufficientBalance_returnsFalse() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 500L);

            assertThat(memberPoint.hasEnoughBalance(1000L)).isFalse();
        }

        @Test
        @DisplayName("잔액이 0이면 false")
        void zeroBalance_returnsFalse() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.create(memberId);

            assertThat(memberPoint.hasEnoughBalance(1L)).isFalse();
        }
    }
}
