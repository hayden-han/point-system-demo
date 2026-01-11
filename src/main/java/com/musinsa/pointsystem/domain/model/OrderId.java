package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 주문번호 Value Object
 * - null/빈 문자열 검증
 * - 불변 객체
 */
@Getter
@EqualsAndHashCode
public final class OrderId {
    private final String value;

    private OrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderIdException();
        }
        this.value = value;
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
