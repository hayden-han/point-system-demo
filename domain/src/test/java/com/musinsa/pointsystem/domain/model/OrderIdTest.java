package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderId 값 객체 테스트")
class OrderIdTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("유효한 주문번호로 생성 성공")
        void createWithValidValue() {
            OrderId orderId = OrderId.of("ORDER-12345");

            assertThat(orderId.value()).isEqualTo("ORDER-12345");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("null 또는 빈 문자열로 생성 시 예외 발생")
        void createWithInvalidValueThrowsException(String value) {
            assertThatThrownBy(() -> OrderId.of(value))
                    .isInstanceOf(InvalidOrderIdException.class);
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값은 동등")
        void equalValues() {
            OrderId a = OrderId.of("ORDER-12345");
            OrderId b = OrderId.of("ORDER-12345");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값은 동등하지 않음")
        void differentValues() {
            OrderId a = OrderId.of("ORDER-12345");
            OrderId b = OrderId.of("ORDER-67890");

            assertThat(a).isNotEqualTo(b);
        }
    }
}
