package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.usecase.CancelEarnPointUseCase;
import com.musinsa.pointsystem.application.usecase.EarnPointUseCase;
import com.musinsa.pointsystem.presentation.dto.request.EarnPointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelEarnPointResponse;
import com.musinsa.pointsystem.presentation.dto.response.EarnPointResponse;
import com.musinsa.pointsystem.presentation.dto.response.ErrorResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Point Earn", description = "포인트 적립 API")
@RestController
@RequestMapping("/api/v1/members/{memberId}/points/earn")
@RequiredArgsConstructor
@Slf4j
public class PointEarnController {

    private final EarnPointUseCase earnPointUseCase;
    private final CancelEarnPointUseCase cancelEarnPointUseCase;
    private final IdempotencySupport idempotencySupport;

    @Operation(
            summary = "포인트 적립",
            description = "회원에게 포인트를 적립합니다.\n\n" +
                    "- 1회 적립 금액: 1원 ~ 100,000원\n" +
                    "- 최대 보유 금액: 10,000,000원\n" +
                    "- 만료일: 기본 365일 (최소 1일 ~ 최대 1,824일)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "적립 성공",
                    content = @Content(schema = @Schema(implementation = EarnPointResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public EarnPointResponse earn(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable UUID memberId,
            @Parameter(description = "멱등성 키 (중복 요청 방지용)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EarnPointRequest request) {

        return idempotencySupport.executeWithIdempotency(idempotencyKey, EarnPointResponse.class, () -> {
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(request.amount())
                    .earnType(request.earnType())
                    .expirationDays(request.expirationDays())
                    .build();

            EarnPointResult result = earnPointUseCase.execute(command);
            return EarnPointResponse.from(result);
        });
    }

    @Operation(
            summary = "적립 취소",
            description = "적립된 포인트를 취소합니다.\n\n" +
                    "- 미사용 적립건만 취소 가능\n" +
                    "- 전체 금액 취소만 가능 (부분 취소 불가)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "적립 취소 성공",
                    content = @Content(schema = @Schema(implementation = CancelEarnPointResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "적립건을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{ledgerId}/cancel")
    public CancelEarnPointResponse cancelEarn(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable UUID memberId,
            @Parameter(description = "취소할 적립건 ID", required = true)
            @PathVariable UUID ledgerId,
            @Parameter(description = "멱등성 키 (중복 요청 방지용)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        return idempotencySupport.executeWithIdempotency(idempotencyKey, CancelEarnPointResponse.class, () -> {
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);
            return CancelEarnPointResponse.from(result);
        });
    }
}
