package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.usecase.CancelEarnPointUseCase;
import com.musinsa.pointsystem.application.usecase.EarnPointUseCase;
import com.musinsa.pointsystem.infra.idempotency.IdempotencyKeyRepository;
import com.musinsa.pointsystem.presentation.dto.request.EarnPointRequest;
import com.musinsa.pointsystem.presentation.dto.response.CancelEarnPointResponse;
import com.musinsa.pointsystem.presentation.dto.response.EarnPointResponse;
import com.musinsa.pointsystem.presentation.dto.response.ErrorResponse;
import com.musinsa.pointsystem.presentation.exception.DuplicateRequestException;
import com.musinsa.pointsystem.presentation.exception.RequestInProgressException;
import com.musinsa.pointsystem.infra.idempotency.IdempotencyKeyRepository.AcquireResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "INVALID_EARN_AMOUNT",
                                            summary = "적립 금액 범위 초과",
                                            value = "{\"code\": \"INVALID_EARN_AMOUNT\", \"message\": \"적립 금액은 1원 이상이어야 합니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "MAX_BALANCE_EXCEEDED",
                                            summary = "최대 보유금액 초과",
                                            value = "{\"code\": \"MAX_BALANCE_EXCEEDED\", \"message\": \"최대 보유 가능 금액(10,000,000원)을 초과합니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "INVALID_EXPIRATION",
                                            summary = "만료일 범위 초과",
                                            value = "{\"code\": \"INVALID_EXPIRATION\", \"message\": \"만료일은 1일 이상이어야 합니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "VALIDATION_ERROR",
                                            summary = "입력값 검증 실패",
                                            value = "{\"code\": \"VALIDATION_ERROR\", \"message\": \"적립 금액은 필수입니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
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
    public EarnPointResponse earn(
            @Parameter(description = "회원 ID", required = true, example = "01938f9a-1234-7abc-def0-123456789abc")
            @PathVariable UUID memberId,
            @Parameter(description = "멱등성 키 (중복 요청 방지용, 선택)", example = "unique-request-id-123")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EarnPointRequest request) {

        // 멱등성 키가 있으면 중복 체크
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            // 이미 처리된 결과가 있으면 반환
            var cachedResult = idempotencyKeyRepository.getResult(idempotencyKey);
            if (cachedResult.isPresent()) {
                try {
                    return objectMapper.readValue(cachedResult.get(), EarnPointResponse.class);
                } catch (Exception e) {
                    log.warn("멱등성 캐시 역직렬화 실패: {}", e.getMessage());
                }
            }

            // 새 요청 처리 시도
            AcquireResult acquireResult = idempotencyKeyRepository.tryAcquire(idempotencyKey);
            switch (acquireResult) {
                case ACQUIRED:
                    // 정상 처리 계속
                    break;
                case ALREADY_COMPLETED:
                    // 결과 조회 후 반환 (race condition으로 캐시 miss 후 완료된 경우)
                    var completedResult = idempotencyKeyRepository.getResult(idempotencyKey);
                    if (completedResult.isPresent()) {
                        try {
                            return objectMapper.readValue(completedResult.get(), EarnPointResponse.class);
                        } catch (Exception e) {
                            log.warn("멱등성 캐시 역직렬화 실패: {}", e.getMessage());
                        }
                    }
                    throw new DuplicateRequestException(idempotencyKey);
                case PROCESSING:
                    // 다른 요청이 처리 중 - 잠시 후 재시도 안내
                    throw new RequestInProgressException(idempotencyKey);
            }
        }

        try {
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(request.amount())
                    .earnType(request.earnType())
                    .expirationDays(request.expirationDays())
                    .build();

            EarnPointResult result = earnPointUseCase.execute(command);
            EarnPointResponse response = EarnPointResponse.from(result);

            // 멱등성 키가 있으면 결과 저장
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                try {
                    idempotencyKeyRepository.saveResult(idempotencyKey, objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.warn("멱등성 결과 저장 실패: {}", e.getMessage());
                }
            }

            return response;
        } catch (Exception e) {
            // 실패 시 멱등성 키 삭제 (재시도 허용)
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyKeyRepository.remove(idempotencyKey);
            }
            throw e;
        }
    }

    @Operation(
            summary = "적립 취소",
            description = "적립된 포인트를 취소합니다.\n\n" +
                    "- 미사용 적립건만 취소 가능\n" +
                    "- 일부/전체 사용된 적립건은 취소 불가\n" +
                    "- 전체 금액 취소만 가능 (부분 취소 불가)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "적립 취소 성공",
                    content = @Content(schema = @Schema(implementation = CancelEarnPointResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "LEDGER_ALREADY_CANCELED",
                                            summary = "이미 취소된 적립건",
                                            value = "{\"code\": \"LEDGER_ALREADY_CANCELED\", \"message\": \"이미 취소된 적립건입니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "LEDGER_ALREADY_USED",
                                            summary = "이미 사용된 적립건",
                                            value = "{\"code\": \"LEDGER_ALREADY_USED\", \"message\": \"이미 사용된 적립건은 취소할 수 없습니다.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                                    )
                            })),
            @ApiResponse(responseCode = "404", description = "적립건을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "LEDGER_NOT_FOUND",
                                    value = "{\"code\": \"LEDGER_NOT_FOUND\", \"message\": \"적립건을 찾을 수 없습니다. ID: 01938f9a-1234-7abc-def0-123456789abc\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                            ))),
            @ApiResponse(responseCode = "503", description = "서버 과부하",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "LOCK_ACQUISITION_FAILED",
                                    value = "{\"code\": \"LOCK_ACQUISITION_FAILED\", \"message\": \"서버가 바쁩니다. 잠시 후 다시 시도해주세요.\", \"timestamp\": \"2024-01-01T12:00:00\"}"
                            )))
    })
    @PostMapping("/{ledgerId}/cancel")
    public CancelEarnPointResponse cancelEarn(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable UUID memberId,
            @Parameter(description = "취소할 적립건 ID", required = true)
            @PathVariable UUID ledgerId) {
        CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                .memberId(memberId)
                .ledgerId(ledgerId)
                .build();

        CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);
        return CancelEarnPointResponse.from(result);
    }
}
