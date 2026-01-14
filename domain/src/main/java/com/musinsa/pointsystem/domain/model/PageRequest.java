package com.musinsa.pointsystem.domain.model;

/**
 * 도메인 레이어의 페이징 요청
 * - Spring Data에 의존하지 않는 순수 도메인 객체
 */
public record PageRequest(
        int page,
        int size
) {
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다: " + page);
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1~100 사이여야 합니다: " + size);
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public long offset() {
        return (long) page * size;
    }
}
