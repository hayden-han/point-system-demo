package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.application.dto.PagedResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 응답 DTO
 * - Application PagedResult를 Presentation 레이어 응답으로 변환
 */
@Schema(description = "페이지네이션 응답")
@Builder
public record PageResponse<T>(

        @Schema(description = "페이지 내용")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int pageNumber,

        @Schema(description = "페이지 크기", example = "20")
        int pageSize,

        @Schema(description = "전체 요소 수", example = "100")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
    public static <T, R> PageResponse<R> from(PagedResult<T> pagedResult, Function<T, R> mapper) {
        List<R> mappedContent = pagedResult.content().stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .content(mappedContent)
                .pageNumber(pagedResult.pageNumber())
                .pageSize(pagedResult.pageSize())
                .totalElements(pagedResult.totalElements())
                .totalPages(pagedResult.totalPages())
                .hasNext(pagedResult.hasNext())
                .hasPrevious(pagedResult.hasPrevious())
                .build();
    }
}
