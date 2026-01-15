package com.musinsa.pointsystem.application.dto;

import java.util.List;

/**
 * Application 레이어의 페이지네이션 결과 DTO
 * - Presentation 레이어가 Domain의 PageResult에 직접 의존하지 않도록 함
 */
public record PagedResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PagedResult<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        boolean hasNext = pageNumber < totalPages - 1;
        boolean hasPrevious = pageNumber > 0;
        return new PagedResult<>(content, pageNumber, pageSize, totalElements, totalPages, hasNext, hasPrevious);
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
