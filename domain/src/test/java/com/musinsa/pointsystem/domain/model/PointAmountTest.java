package com.musinsa.pointsystem.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PointAmount 값 객체 테스트")
class PointAmountTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("양수 금액으로 생성 성공")
        void createWithPositiveValue() {
            PointAmount amount = PointAmount.of(1000L);

            assertThat(amount.value()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("0으로 생성 시 ZERO 상수 반환")
        void createWithZeroReturnsSingleton() {
            PointAmount amount = PointAmount.of(0L);

            assertThat(amount).isSameAs(PointAmount.ZERO);
        }

        @Test
        @DisplayName("음수 금액으로 생성 시 예외 발생")
        void createWithNegativeValueThrowsException() {
            assertThatThrownBy(() -> PointAmount.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }

        @Test
        @DisplayName("최대값(1000억)으로 생성 성공")
        void createWithMaxValue() {
            PointAmount amount = PointAmount.of(PointAmount.MAX_VALUE);

            assertThat(amount.value()).isEqualTo(100_000_000_000L);
        }

        @Test
        @DisplayName("최대값 초과 시 예외 발생")
        void createExceedingMaxValueThrowsException() {
            assertThatThrownBy(() -> PointAmount.of(PointAmount.MAX_VALUE + 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시스템 최대값");
        }
    }

    @Nested
    @DisplayName("연산")
    class Operations {

        @Test
        @DisplayName("덧셈")
        void add() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(500L);

            PointAmount result = a.add(b);

            assertThat(result.value()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("뺄셈")
        void subtract() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(300L);

            PointAmount result = a.subtract(b);

            assertThat(result.value()).isEqualTo(700L);
        }

        @Test
        @DisplayName("뺄셈 결과가 음수면 예외 발생")
        void subtractResultNegativeThrowsException() {
            PointAmount a = PointAmount.of(100L);
            PointAmount b = PointAmount.of(200L);

            assertThatThrownBy(() -> a.subtract(b))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("덧셈 결과가 최대값 초과 시 예외 발생")
        void addResultExceedingMaxValueThrowsException() {
            PointAmount a = PointAmount.of(PointAmount.MAX_VALUE);
            PointAmount b = PointAmount.of(1L);

            assertThatThrownBy(() -> a.add(b))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("허용 범위를 벗어납니다");
        }

        @Test
        @DisplayName("덧셈 결과가 정확히 최대값이면 성공")
        void addResultExactlyMaxValue() {
            PointAmount a = PointAmount.of(PointAmount.MAX_VALUE - 1000L);
            PointAmount b = PointAmount.of(1000L);

            PointAmount result = a.add(b);

            assertThat(result.value()).isEqualTo(PointAmount.MAX_VALUE);
        }

        @Test
        @DisplayName("0 + 0 = 0")
        void addZeroToZero() {
            PointAmount result = PointAmount.ZERO.add(PointAmount.ZERO);

            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("min - 작은 값 반환")
        void minReturnsSmaller() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(500L);

            assertThat(a.min(b)).isEqualTo(b);
            assertThat(b.min(a)).isEqualTo(b);
        }

        @Test
        @DisplayName("max - 큰 값 반환")
        void maxReturnsLarger() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(500L);

            assertThat(a.max(b)).isEqualTo(a);
            assertThat(b.max(a)).isEqualTo(a);
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("isGreaterThan")
        void isGreaterThan() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(500L);

            assertThat(a.isGreaterThan(b)).isTrue();
            assertThat(b.isGreaterThan(a)).isFalse();
            assertThat(a.isGreaterThan(a)).isFalse();
        }

        @Test
        @DisplayName("isLessThan")
        void isLessThan() {
            PointAmount a = PointAmount.of(500L);
            PointAmount b = PointAmount.of(1000L);

            assertThat(a.isLessThan(b)).isTrue();
            assertThat(b.isLessThan(a)).isFalse();
        }

        @Test
        @DisplayName("isGreaterThanOrEqual")
        void isGreaterThanOrEqual() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(1000L);
            PointAmount c = PointAmount.of(500L);

            assertThat(a.isGreaterThanOrEqual(b)).isTrue();
            assertThat(a.isGreaterThanOrEqual(c)).isTrue();
            assertThat(c.isGreaterThanOrEqual(a)).isFalse();
        }

        @Test
        @DisplayName("isLessThanOrEqual")
        void isLessThanOrEqual() {
            PointAmount a = PointAmount.of(500L);
            PointAmount b = PointAmount.of(500L);
            PointAmount c = PointAmount.of(1000L);

            assertThat(a.isLessThanOrEqual(b)).isTrue();
            assertThat(a.isLessThanOrEqual(c)).isTrue();
            assertThat(c.isLessThanOrEqual(a)).isFalse();
        }

        @Test
        @DisplayName("compareTo")
        void compareTo() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(500L);
            PointAmount c = PointAmount.of(1000L);

            assertThat(a.compareTo(b)).isPositive();
            assertThat(b.compareTo(a)).isNegative();
            assertThat(a.compareTo(c)).isZero();
        }
    }

    @Nested
    @DisplayName("상태 확인")
    class StateCheck {

        @Test
        @DisplayName("isZero")
        void isZero() {
            assertThat(PointAmount.ZERO.isZero()).isTrue();
            assertThat(PointAmount.of(0L).isZero()).isTrue();
            assertThat(PointAmount.of(100L).isZero()).isFalse();
        }

        @Test
        @DisplayName("isPositive")
        void isPositive() {
            assertThat(PointAmount.of(100L).isPositive()).isTrue();
            assertThat(PointAmount.ZERO.isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값은 동등")
        void equalValues() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(1000L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값은 동등하지 않음")
        void differentValues() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(2000L);

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("경계: 0 - 최소 유효값")
        void boundary_zero() {
            PointAmount amount = PointAmount.of(0L);
            assertThat(amount.isZero()).isTrue();
            assertThat(amount.isPositive()).isFalse();
        }

        @Test
        @DisplayName("경계: 1 - 최소 양수값")
        void boundary_one() {
            PointAmount amount = PointAmount.of(1L);
            assertThat(amount.isPositive()).isTrue();
            assertThat(amount.isZero()).isFalse();
        }

        @Test
        @DisplayName("경계: -1 - 최소 음수값 (예외)")
        void boundary_negativeOne() {
            assertThatThrownBy(() -> PointAmount.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }

        @Test
        @DisplayName("경계: MAX_VALUE - 정확히 최대값")
        void boundary_maxValue() {
            PointAmount amount = PointAmount.of(PointAmount.MAX_VALUE);
            assertThat(amount.value()).isEqualTo(100_000_000_000L);
        }

        @Test
        @DisplayName("경계: MAX_VALUE - 1 - 최대값 바로 아래")
        void boundary_maxValueMinusOne() {
            PointAmount amount = PointAmount.of(PointAmount.MAX_VALUE - 1);
            assertThat(amount.value()).isEqualTo(99_999_999_999L);
        }

        @Test
        @DisplayName("경계: MAX_VALUE + 1 - 최대값 초과 (예외)")
        void boundary_maxValuePlusOne() {
            assertThatThrownBy(() -> PointAmount.of(PointAmount.MAX_VALUE + 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시스템 최대값");
        }

        @Test
        @DisplayName("경계: 덧셈으로 MAX_VALUE 정확히 도달")
        void boundary_addToExactMax() {
            PointAmount a = PointAmount.of(PointAmount.MAX_VALUE - 1);
            PointAmount b = PointAmount.of(1L);

            PointAmount result = a.add(b);

            assertThat(result.value()).isEqualTo(PointAmount.MAX_VALUE);
        }

        @Test
        @DisplayName("경계: 덧셈으로 MAX_VALUE 초과 (예외)")
        void boundary_addExceedsMax() {
            PointAmount a = PointAmount.of(PointAmount.MAX_VALUE);
            PointAmount b = PointAmount.of(1L);

            assertThatThrownBy(() -> a.add(b))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("허용 범위를 벗어납니다");
        }

        @Test
        @DisplayName("경계: 뺄셈으로 정확히 0 도달")
        void boundary_subtractToZero() {
            PointAmount a = PointAmount.of(100L);
            PointAmount b = PointAmount.of(100L);

            PointAmount result = a.subtract(b);

            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("경계: 뺄셈으로 음수 (예외)")
        void boundary_subtractToNegative() {
            PointAmount a = PointAmount.of(100L);
            PointAmount b = PointAmount.of(101L);

            assertThatThrownBy(() -> a.subtract(b))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("경계: negate()는 음수 long 반환")
        void boundary_negate() {
            PointAmount amount = PointAmount.of(1000L);
            assertThat(amount.negate()).isEqualTo(-1000L);
        }

        @Test
        @DisplayName("경계: negate() - 0의 부호 반전")
        void boundary_negateZero() {
            assertThat(PointAmount.ZERO.negate()).isEqualTo(0L);
        }

        @Test
        @DisplayName("경계: compareTo - 동일값")
        void boundary_compareToEqual() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(1000L);
            assertThat(a.compareTo(b)).isZero();
        }

        @Test
        @DisplayName("경계: compareTo - 최소차이 (1)")
        void boundary_compareToMinDiff() {
            PointAmount a = PointAmount.of(1000L);
            PointAmount b = PointAmount.of(1001L);
            assertThat(a.compareTo(b)).isNegative();
            assertThat(b.compareTo(a)).isPositive();
        }
    }
}
