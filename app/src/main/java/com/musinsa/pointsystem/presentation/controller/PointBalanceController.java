package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.application.dto.PageQuery;
import com.musinsa.pointsystem.application.dto.PagedResult;
import com.musinsa.pointsystem.application.dto.PointBalanceResult;
import com.musinsa.pointsystem.application.dto.PointHistoryResult;
import com.musinsa.pointsystem.application.usecase.GetPointBalanceUseCase;
import com.musinsa.pointsystem.application.usecase.GetPointHistoryUseCase;
import com.musinsa.pointsystem.presentation.dto.response.PageResponse;
import com.musinsa.pointsystem.presentation.dto.response.PointBalanceResponse;
import com.musinsa.pointsystem.presentation.dto.response.PointHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Point Balance", description = "포인트 잔액/이력 조회 API")
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointBalanceController {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";

    private final GetPointBalanceUseCase getPointBalanceUseCase;
    private final GetPointHistoryUseCase getPointHistoryUseCase;

    @Operation(
            summary = "포인트 잔액 조회",
            description = "회원의 현재 포인트 잔액을 조회합니다.\n\n" +
                    "- 존재하지 않는 회원은 잔액 0으로 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PointBalanceResponse.class)))
    })
    @GetMapping
    public PointBalanceResponse getBalance(
            @Parameter(description = "회원 ID (Gateway에서 주입)", required = true)
            @RequestHeader(MEMBER_ID_HEADER) UUID memberId) {
        PointBalanceResult result = getPointBalanceUseCase.execute(memberId);
        return PointBalanceResponse.from(result);
    }

    @Operation(
            summary = "포인트 변동 이력 조회",
            description = "회원의 포인트 변동 이력을 페이지네이션으로 조회합니다.\n\n" +
                    "**변동 유형:**\n" +
                    "- `EARN`: 적립 (양수)\n" +
                    "- `EARN_CANCEL`: 적립 취소 (음수)\n" +
                    "- `USE`: 사용 (음수)\n" +
                    "- `USE_CANCEL`: 사용 취소 (양수)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/history")
    public PageResponse<PointHistoryResponse> getHistory(
            @Parameter(description = "회원 ID (Gateway에서 주입)", required = true)
            @RequestHeader(MEMBER_ID_HEADER) UUID memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PagedResult<PointHistoryResult> history = getPointHistoryUseCase.execute(memberId, pageQuery);
        return PageResponse.from(history, PointHistoryResponse::from);
    }
}
