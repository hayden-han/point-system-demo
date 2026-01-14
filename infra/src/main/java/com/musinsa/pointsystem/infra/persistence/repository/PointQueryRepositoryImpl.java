package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PageRequest;
import com.musinsa.pointsystem.domain.model.PageResult;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointHistory;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PointQueryRepository 구현체
 *
 * <p>Aggregate 로드 없이 DB에서 직접 조회하여 성능 최적화.
 */
@Repository
@RequiredArgsConstructor
public class PointQueryRepositoryImpl implements PointQueryRepository {

    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public PointAmount getTotalBalance(UUID memberId, LocalDateTime now) {
        Long balance = pointLedgerJpaRepository.sumAvailableAmount(memberId, now);
        return PointAmount.of(balance != null ? balance : 0L);
    }

    @Override
    public int getAvailableLedgerCount(UUID memberId, LocalDateTime now) {
        return pointLedgerJpaRepository.countAvailableLedgers(memberId, now);
    }

    @Override
    public PageResult<PointHistory> getHistory(UUID memberId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size()
        );

        Page<LedgerEntryEntity> page = ledgerEntryJpaRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        return PageResult.of(
                page.getContent().stream()
                        .map(this::toPointHistory)
                        .toList(),
                pageRequest.page(),
                pageRequest.size(),
                page.getTotalElements()
        );
    }

    private PointHistory toPointHistory(LedgerEntryEntity entity) {
        return PointHistory.of(
                entity.getId(),
                entity.getLedgerId(),
                entity.getType(),
                entity.getAmount(),
                entity.getOrderId(),
                entity.getCreatedAt()
        );
    }
}
