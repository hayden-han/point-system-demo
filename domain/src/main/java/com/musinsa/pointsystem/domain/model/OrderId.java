package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;

/**
 * 주문번호 Value Object
 * - null/빈 문자열 검증
 * - 불변 record
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderIdException();
        }
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    // 기존 코드 호환성을 위해 getValue() 추가
    public String getValue() {
        return value;
    }
}
