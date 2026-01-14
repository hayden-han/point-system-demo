package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.domain.model.PointAmount;
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
    public Page<PointHistoryProjection> getHistory(UUID memberId, Pageable pageable) {
        return ledgerEntryJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(this::toProjection);
    }

    private PointHistoryProjection toProjection(LedgerEntryEntity entity) {
        return new PointHistoryProjection() {
            @Override
            public UUID getEntryId() {
                return entity.getId();
            }

            @Override
            public UUID getLedgerId() {
                return entity.getLedgerId();
            }

            @Override
            public String getType() {
                return entity.getType().name();
            }

            @Override
            public long getAmount() {
                return entity.getAmount();
            }

            @Override
            public String getOrderId() {
                return entity.getOrderId();
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return entity.getCreatedAt();
            }
        };
    }
}
