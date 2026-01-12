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
}
