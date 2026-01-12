package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.application.usecase.CancelUsePointUseCase;
import com.musinsa.pointsystem.application.usecase.UsePointUseCase;
import com.musinsa.pointsystem.presentation.dto.request.CancelUsePointRequest;
import com.musinsa.pointsystem.presentation.dto.request.UsePointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelUsePointResponse;
import com.musinsa.pointsystem.presentation.dto.response.ErrorResponse;
import com.musinsa.pointsystem.presentation.dto.response.UsePointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Point Use", description = "포인트 사용 API")
@RestController
@RequestMapping("/api/v1/members/{memberId}/points/use")
@RequiredArgsConstructor
public class PointUseController {

    private final UsePointUseCase usePointUseCase;
    private final CancelUsePointUseCase cancelUsePointUseCase;

    @Operation(
            summary = "포인트 사용",
            description = "회원의 포인트를 사용합니다.\n\n" +
                    "**사용 우선순위:**\n" +
                    "1. 수기 지급 포인트 우선\n" +
                    "2. 만료일 짧은 순서\n\n" +
                    "**주의사항:**\n" +
                    "- 잔액 부족 시 사용 불가\n" +
                    "- 주문번호(orderId) 필수"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 성공",
                    content = @Content(schema = @Schema(implementation = UsePointResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "INSUFFICIENT_POINT",
                                            summary = "잔액 부족",
                                            value = "{\"code\": \"INSUFFICIENT_POINT\", \"message\": \"잔액이 부족합니다. 요청: 10000원, 잔액: 5000원\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "VALIDATION_ERROR",
                                            summary = "입력값 검증 실패",
                                            value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"주문번호는 필수입니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    )
                            })),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "LOCK_ACQUISITION_FAILED",
                                    value = "{\"code\": \"LOCK_ACQUISITION_FAILED\", \"message\": \"서버가 바쁩니다. 잠시 후 다시 시도해주세요.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                            )))
    })
    @PostMapping
    public UsePointResponse use(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable UUID memberId,
            @Valid @RequestBody UsePointRequest request) {
        UsePointCommand command = UsePointCommand.builder()
                .memberId(memberId)
                .amount(request.amount())
                .orderId(request.orderId())
                .build();

        UsePointResult result = usePointUseCase.execute(command);
        return UsePointResponse.from(result);
    }

    @Operation(
            summary = "사용 취소",
            description = "사용된 포인트를 취소합니다.\n\n" +
                    "**취소 정책:**\n" +
                    "- 전체 취소 또는 부분 취소 가능\n" +
                    "- 만료일이 남은 적립건: 원래 적립건으로 복구\n" +
                    "- 만료된 적립건: 새로운 적립건 생성 (기본 만료일 365일)\n\n" +
                    "**취소 우선순위:**\n" +
                    "- 만료일 긴 적립건부터 취소"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 취소 성공",
                    content = @Content(schema = @Schema(implementation = CancelUsePointResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "INVALID_CANCEL_AMOUNT",
                                            summary = "취소 가능 금액 초과",
                                            value = "{\"code\": \"INVALID_CANCEL_AMOUNT\", \"message\": \"취소 가능 금액(5000원)을 초과했습니다. 요청: 10000원\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "VALIDATION_ERROR",
                                            summary = "입력값 검증 실패",
                                            value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"원본 트랜잭션 ID는 필수입니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    )
                            })),
            @ApiResponse(responseCode = "404", description = "트랜잭션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "TRANSACTION_NOT_FOUND",
                                    value = "{\"code\": \"TRANSACTION_NOT_FOUND\", \"message\": \"트랜잭션을 찾을 수 없습니다. ID: 01938f9a-1234-7abc-def0-123456789abc\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                            ))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "LOCK_ACQUISITION_FAILED",
                                    value = "{\"code\": \"LOCK_ACQUISITION_FAILED\", \"message\": \"서버가 바쁩니다. 잠시 후 다시 시도해주세요.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                            )))
    })
    @PostMapping("/cancel")
    public CancelUsePointResponse cancelUse(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable UUID memberId,
            @Valid @RequestBody CancelUsePointRequest request) {
        CancelUsePointCommand command = CancelUsePointCommand.builder()
                .memberId(memberId)
                .transactionId(request.transactionId())
                .cancelAmount(request.cancelAmount())
                .build();

        CancelUsePointResult result = cancelUsePointUseCase.execute(command);
        return CancelUsePointResponse.from(result);
    }
}
