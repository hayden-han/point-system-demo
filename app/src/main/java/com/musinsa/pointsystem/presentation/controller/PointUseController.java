package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.usecase.CancelUsePointUseCase;
import com.musinsa.pointsystem.application.usecase.UsePointUseCase;
import com.musinsa.pointsystem.presentation.dto.request.CancelUsePointRequest;
import com.musinsa.pointsystem.presentation.dto.request.UsePointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelUsePointResponse;
import com.musinsa.pointsystem.presentation.dto.response.ErrorResponse;
import com.musinsa.pointsystem.presentation.dto.response.UsePointResponse;
import com.musinsa.pointsystem.presentation.support.IdempotencySupport;
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
@RequestMapping("/api/v1/points/use")
@RequiredArgsConstructor
public class PointUseController {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";

    private final UsePointUseCase usePointUseCase;
    private final CancelUsePointUseCase cancelUsePointUseCase;
    private final IdempotencySupport idempotencySupport;

    @Operation(
            summary = "포인트 사용",
            description = "회원의 포인트를 사용합니다.\n\n" +
                    "**사용 우선순위:**\n" +
                    "1. 수기 지급 포인트 우선\n" +
                    "2. 만료일 짧은 순서"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 성공",
                    content = @Content(schema = @Schema(implementation = UsePointResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public UsePointResponse use(
            @Parameter(description = "회원 ID (Gateway에서 주입)", required = true)
            @RequestHeader(MEMBER_ID_HEADER) UUID memberId,
            @Parameter(description = "멱등성 키 (중복 요청 방지용)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody UsePointRequest request) {

        UsePointCommand command = UsePointCommand.builder()
                .memberId(memberId)
                .amount(request.amount())
                .orderId(request.orderId())
                .build();

        return idempotencySupport.execute(
                idempotencyKey,
                UsePointResponse.class,
                () -> usePointUseCase.execute(command),
                UsePointResponse::from
        );
    }

    @Operation(
            summary = "사용 취소",
            description = "사용된 포인트를 취소합니다.\n\n" +
                    "**취소 정책:**\n" +
                    "- 전체 취소 또는 부분 취소 가능\n" +
                    "- 만료일이 남은 적립건: 원래 적립건으로 복구\n" +
                    "- 만료된 적립건: 새로운 적립건 생성"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 취소 성공",
                    content = @Content(schema = @Schema(implementation = CancelUsePointResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "트랜잭션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/cancel")
    public CancelUsePointResponse cancelUse(
            @Parameter(description = "회원 ID (Gateway에서 주입)", required = true)
            @RequestHeader(MEMBER_ID_HEADER) UUID memberId,
            @Parameter(description = "멱등성 키 (중복 요청 방지용)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CancelUsePointRequest request) {

        CancelUsePointCommand command = CancelUsePointCommand.builder()
                .memberId(memberId)
                .orderId(request.orderId())
                .cancelAmount(request.cancelAmount())
                .build();

        return idempotencySupport.execute(
                idempotencyKey,
                CancelUsePointResponse.class,
                () -> cancelUsePointUseCase.execute(command),
                CancelUsePointResponse::from
        );
    }
}
