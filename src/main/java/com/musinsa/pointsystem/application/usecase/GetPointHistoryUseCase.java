package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.PageQuery;
import com.musinsa.pointsystem.application.dto.PagedResult;
import com.musinsa.pointsystem.application.dto.PointHistoryResult;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.repository.LedgerEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPointHistoryUseCase {

    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Transactional(readOnly = true)
    public PagedResult<PointHistoryResult> execute(UUID memberId, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(pageQuery.pageNumber(), pageQuery.pageSize());
        Page<LedgerEntryEntity> page = ledgerEntryJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageRequest);

        List<PointHistoryResult> results = page.getContent().stream()
                .map(this::toResult)
                .toList();

        return PagedResult.of(
                results,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    private PointHistoryResult toResult(LedgerEntryEntity entity) {
        return PointHistoryResult.builder()
                .entryId(entity.getId())
                .ledgerId(entity.getLedgerId())
                .type(entity.getType().name())
                .amount(entity.getAmount())
                .orderId(entity.getOrderId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
