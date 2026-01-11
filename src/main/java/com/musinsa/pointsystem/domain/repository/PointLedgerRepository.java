package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointLedger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointLedgerRepository {
    PointLedger save(PointLedger pointLedger);
    Optional<PointLedger> findById(UUID id);
    List<PointLedger> findAllById(List<UUID> ids);
    List<PointLedger> findAvailableByMemberId(UUID memberId);
    List<PointLedger> saveAll(List<PointLedger> pointLedgers);
}
