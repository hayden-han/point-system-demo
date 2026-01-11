package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.domain.model.PageResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 응답 DTO
 * - 도메인 PageResult를 Presentation 레이어 응답으로 변환
 */
@Getter
@Builder
public class PageResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public static <T, R> PageResponse<R> from(PageResult<T> pageResult, Function<T, R> mapper) {
        List<R> mappedContent = pageResult.content().stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .content(mappedContent)
                .pageNumber(pageResult.pageNumber())
                .pageSize(pageResult.pageSize())
                .totalElements(pageResult.totalElements())
                .totalPages(pageResult.totalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}
