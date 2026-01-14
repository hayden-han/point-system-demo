package com.musinsa.pointsystem.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "에러 응답")
@Builder
public record ErrorResponse(

        @Schema(description = "에러 코드", example = "INSUFFICIENT_POINT")
        String code,

        @Schema(description = "에러 메시지", example = "잔액이 부족합니다.")
        String message,

        @Schema(description = "발생 시각", example = "2024-01-01T12:00:00")
        LocalDateTime timestamp,

        @Schema(description = "트레이스 ID (문제 추적용)", example = "550e8400-e29b-41d4-a716-446655440000")
        String traceId
) {
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .traceId(getOrGenerateTraceId())
                .build();
    }

    private static String getOrGenerateTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
