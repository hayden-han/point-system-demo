package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PageQuery;
import com.musinsa.pointsystem.application.dto.PagedResult;
import com.musinsa.pointsystem.application.dto.PointTransactionResult;
import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointTransaction;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPointHistoryUseCase {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public PagedResult<PointTransactionResult> execute(UUID memberId, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(pageQuery.pageNumber(), pageQuery.pageSize());
        PageResult<PointTransaction> pageResult = pointTransactionRepository.findByMemberId(memberId, pageRequest);

        List<PointTransactionResult> results = pageResult.content().stream()
                .map(this::toResult)
                .toList();

        return PagedResult.of(
                results,
                pageResult.pageNumber(),
                pageResult.pageSize(),
                pageResult.totalElements()
        );
    }

    private PointTransactionResult toResult(PointTransaction transaction) {
        return PointTransactionResult.builder()
                .transactionId(transaction.getId())
                .memberId(transaction.getMemberId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount().getValue())
                .orderId(transaction.getOrderId() != null ? transaction.getOrderId().getValue() : null)
                .relatedTransactionId(transaction.getRelatedTransactionId())
                .ledgerId(transaction.getLedgerId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
