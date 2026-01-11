package com.musinsa.pointsystem.domain.model;

import java.util.List;

/**
 * 도메인 계층의 페이지네이션 결과 추상화
 * - Spring Framework 의존성 없이 페이징 결과를 표현
 * - 불변 객체 (record)
 */
public record PageResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new PageResult<>(content, pageNumber, pageSize, totalElements, totalPages);
    }

    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
