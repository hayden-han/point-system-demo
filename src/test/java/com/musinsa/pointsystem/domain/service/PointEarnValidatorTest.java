package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.fixture.MemberPointFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointEarnValidatorTest {

    private PointEarnValidator validator;
    private EarnPolicyConfig policy;

    @BeforeEach
    void setUp() {
        validator = new PointEarnValidator();
        policy = EarnPolicyConfig.builder()
                .minAmount(1L)
                .maxAmount(100000L)
                .maxBalance(10000000L)
                .defaultExpirationDays(365)
                .minExpirationDays(1)
                .maxExpirationDays(1824)
                .build();
    }

    @Nested
    @DisplayName("금액 검증")
    class ValidateAmountTest {

        @Test
        @DisplayName("최소 금액 이상이면 통과")
        void minimumAmount_shouldPass() {
            assertThatCode(() -> validator.validateAmount(1L, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최대 금액 이하이면 통과")
        void maximumAmount_shouldPass() {
            assertThatCode(() -> validator.validateAmount(100000L, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 금액 미만이면 예외 발생")
        void belowMinimum_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateAmount(0L, policy))
                    .isInstanceOf(InvalidEarnAmountException.class);
        }

        @Test
        @DisplayName("최대 금액 초과이면 예외 발생")
        void aboveMaximum_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateAmount(100001L, policy))
                    .isInstanceOf(InvalidEarnAmountException.class);
        }
    }

    @Nested
    @DisplayName("최대 보유금액 검증")
    class ValidateMaxBalanceTest {

        @Test
        @DisplayName("적립 후에도 최대 보유금액 이하면 통과")
        void withinMaxBalance_shouldPass() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 5000000L);

            assertThatCode(() -> validator.validateMaxBalance(memberPoint, 4000000L, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("적립 후 최대 보유금액 초과시 예외 발생")
        void exceedsMaxBalance_shouldThrowException() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9500000L);

            assertThatThrownBy(() -> validator.validateMaxBalance(memberPoint, 600000L, policy))
                    .isInstanceOf(MaxBalanceExceededException.class);
        }

        @Test
        @DisplayName("적립 후 정확히 최대 보유금액이면 통과")
        void exactlyMaxBalance_shouldPass() {
            UUID memberId = UuidGenerator.generate();
            MemberPoint memberPoint = MemberPointFixture.createWithBalance(memberId, 9000000L);

            assertThatCode(() -> validator.validateMaxBalance(memberPoint, 1000000L, policy))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("만료일 검증")
    class ValidateExpirationDaysTest {

        @Test
        @DisplayName("null이면 검증 건너뜀")
        void nullDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(null, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 만료일 이상이면 통과")
        void minimumDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(1, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최대 만료일 이하이면 통과")
        void maximumDays_shouldPass() {
            assertThatCode(() -> validator.validateExpirationDays(1824, policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 만료일 미만이면 예외 발생")
        void belowMinimumDays_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateExpirationDays(0, policy))
                    .isInstanceOf(InvalidExpirationException.class);
        }

        @Test
        @DisplayName("최대 만료일 초과이면 예외 발생")
        void aboveMaximumDays_shouldThrowException() {
            assertThatThrownBy(() -> validator.validateExpirationDays(1825, policy))
                    .isInstanceOf(InvalidExpirationException.class);
        }
    }
}
