package com.musinsa.pointsystem.application.dto;

/**
 * Application 레이어의 페이지네이션 요청 DTO
 * - Presentation 레이어가 Domain의 PageRequest에 직접 의존하지 않도록 함
 */
public record PageQuery(
        int pageNumber,
        int pageSize
) {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }

    public static PageQuery of(int pageNumber, int pageSize) {
        return new PageQuery(pageNumber, pageSize);
    }

    public static PageQuery firstPage() {
        return new PageQuery(0, DEFAULT_PAGE_SIZE);
    }
}
