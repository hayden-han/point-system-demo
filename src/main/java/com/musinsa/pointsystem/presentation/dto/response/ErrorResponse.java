package com.musinsa.pointsystem.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "에러 응답")
@Getter
@Builder
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "INSUFFICIENT_POINT")
    private final String code;

    @Schema(description = "에러 메시지", example = "잔액이 부족합니다.")
    private final String message;

    @Schema(description = "발생 시각", example = "2024-01-01T12:00:00")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
