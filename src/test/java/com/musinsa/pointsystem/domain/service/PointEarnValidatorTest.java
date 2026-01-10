package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.fixture.MemberPointFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointEarnValidatorTest {

    private PointEarnValidator validator;

    private static final Long MIN_AMOUNT = 1L;
    private static final Long MAX_AMOUNT = 100000L;
    private static final Long MAX_BALANCE = 10000000L;
    private static final Long MIN_DAYS = 1L;
    private static final Long MAX_DAYS = 1824L;

    @BeforeEach
    void setUp() {
        validator = new PointEarnValidator();
    }

    @Nested
    @DisplayName("금액 검증")
    class ValidateAmountTest {

        @Test
        @DisplayName("최소 금액 이상이면 통과")
        void minimumAmount_shouldPass() {
            assertThatCode(() -> validator.validateAmount(1L, MIN_AMOUNT, MAX_AMOUNT))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최대 금액 이하이면 통과")
        void maximumAmount_shouldPass() {
            assertThatCode(() -> validator.validateAmount(100000L, MIN_AMOUNT, MAX_AMOUNT))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 금액 미만이면 예외 발생")
        void belowMinimum_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateAmount(0L, MIN_AMOUNT, MAX_AMOUNT))
                    .isInstanceOf(InvalidEarnAmountException.class);
        }

        @Test
        @DisplayName("최대 금액 초과이면 예외 발생")
        void aboveMaximum_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateAmount(100001L, MIN_AMOUNT, MAX_AMOUNT))
                    .isInstanceOf(InvalidEarnAmountException.class);
        }
    }

    @Nested
    @DisplayName("최대 보유금액 검증")
    class ValidateMaxBalanceTest {

        @Test
        @DisplayName("적립 후에도 최대 보유금액 이하면 통과")
        void withinMaxBalance_shouldPass() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 5000000L);

            assertThatCode(() -> validator.validateMaxBalance(memberPoint, 4000000L, MAX_BALANCE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("적립 후 최대 보유금액 초과시 예외 발생")
        void exceedsMaxBalance_shouldThrowException() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 9500000L);

            assertThatThrownBy(() -> validator.validateMaxBalance(memberPoint, 600000L, MAX_BALANCE))
                    .isInstanceOf(MaxBalanceExceededException.class);
        }

        @Test
        @DisplayName("적립 후 정확히 최대 보유금액이면 통과")
        void exactlyMaxBalance_shouldPass() {
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(1L, 9000000L);

            assertThatCode(() -> validator.validateMaxBalance(memberPoint, 1000000L, MAX_BALANCE))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("만료일 검증")
    class ValidateExpirationDaysTest {

        @Test
        @DisplayName("null이면 검증 건너뜀")
        void nullDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(null, MIN_DAYS, MAX_DAYS))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 만료일 이상이면 통과")
        void minimumDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(1, MIN_DAYS, MAX_DAYS))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최대 만료일 이하이면 통과")
        void maximumDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(1824, MIN_DAYS, MAX_DAYS))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 만료일 미만이면 예외 발생")
        void belowMinimumDays_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateExpirationDays(0, MIN_DAYS, MAX_DAYS))
                    .isInstanceOf(InvalidExpirationException.class);
        }

        @Test
        @DisplayName("최대 만료일 초과이면 예외 발생")
        void aboveMaximumDays_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateExpirationDays(1825, MIN_DAYS, MAX_DAYS))
                    .isInstanceOf(InvalidExpirationException.class);
        }
    }
}
