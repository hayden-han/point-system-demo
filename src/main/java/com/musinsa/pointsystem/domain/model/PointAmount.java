package com.musinsa.pointsystem.domain.model;

/**
 * 포인트 금액을 나타내는 Value Object.
 * - 불변(immutable) record
 * - 0 이상의 값만 허용
 * - 금액 연산 메서드 제공
 */
public record PointAmount(long value) implements Comparable<PointAmount> {

    public static final PointAmount ZERO = new PointAmount(0L);

    public PointAmount {
        if (value < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + value);
        }
    }

    public static PointAmount of(long value) {
        if (value == 0) {
            return ZERO;
        }
        return new PointAmount(value);
    }

    // record의 getter는 value()로 접근하지만, 기존 코드 호환성을 위해 getValue() 추가
    public long getValue() {
        return value;
    }

    public PointAmount add(PointAmount other) {
        return new PointAmount(this.value + other.value);
    }

    public PointAmount subtract(PointAmount other) {
        return new PointAmount(this.value - other.value);
    }

    public boolean isGreaterThan(PointAmount other) {
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqual(PointAmount other) {
        return this.value >= other.value;
    }

    public boolean isLessThan(PointAmount other) {
        return this.value < other.value;
    }

    public boolean isLessThanOrEqual(PointAmount other) {
        return this.value <= other.value;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isPositive() {
        return this.value > 0;
    }

    public PointAmount min(PointAmount other) {
        return this.value <= other.value ? this : other;
    }

    public PointAmount max(PointAmount other) {
        return this.value >= other.value ? this : other;
    }

    @Override
    public int compareTo(PointAmount other) {
        return Long.compare(this.value, other.value);
    }
}
