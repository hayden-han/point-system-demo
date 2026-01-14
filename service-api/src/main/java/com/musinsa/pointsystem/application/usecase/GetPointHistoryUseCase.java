package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PageQuery;
import com.musinsa.pointsystem.application.dto.PagedResult;
import com.musinsa.pointsystem.application.dto.PointHistoryResult;
import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointHistory;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 포인트 변동 이력 조회 UseCase
 *
 * <p>최적화: Aggregate 로드 없이 DB에서 직접 페이징 조회.
 * 조회 전용 PointQueryRepository를 사용하여 성능 최적화.
 */
@Service
@RequiredArgsConstructor
public class GetPointHistoryUseCase {

    private final PointQueryRepository pointQueryRepository;

    @Transactional(readOnly = true)
    public PagedResult<PointHistoryResult> execute(UUID memberId, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(pageQuery.pageNumber(), pageQuery.pageSize());
        PageResult<PointHistory> pageResult = pointQueryRepository.getHistory(memberId, pageRequest);

        List<PointHistoryResult> results = pageResult.content().stream()
                .map(this::toResult)
                .toList();

        return PagedResult.of(
                results,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements()
        );
    }

    private PointHistoryResult toResult(PointHistory history) {
        return PointHistoryResult.builder()
                .entryId(history.entryId())
                .ledgerId(history.ledgerId())
                .type(history.type().name())
                .amount(history.amount())
                .orderId(history.orderId())
                .createdAt(history.createdAt())
                .build();
    }
}
