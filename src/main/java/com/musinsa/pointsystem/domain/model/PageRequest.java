package com.musinsa.pointsystem.domain.model;

/**
 * 도메인 계층의 페이지네이션 요청 추상화
 * - Spring Framework 의존성 없이 페이징 요청을 표현
 * - 불변 객체 (record)
 */
public record PageRequest(
        int pageNumber,
        int pageSize
) {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public PageRequest {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다: " + pageNumber);
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다: " + pageSize);
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }

    public static PageRequest of(int pageNumber, int pageSize) {
        return new PageRequest(pageNumber, pageSize);
    }

    public static PageRequest firstPage() {
        return new PageRequest(0, DEFAULT_PAGE_SIZE);
    }

    public static PageRequest firstPage(int pageSize) {
        return new PageRequest(0, pageSize);
    }

    public long getOffset() {
        return (long) pageNumber * pageSize;
    }
}
