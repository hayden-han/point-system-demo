package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointLedger;

import java.util.List;
import java.util.Optional;

public interface PointLedgerRepository {
    PointLedger save(PointLedger pointLedger);
    Optional<PointLedger> findById(Long id);
    List<PointLedger> findAvailableByMemberId(Long memberId);
    List<PointLedger> saveAll(List<PointLedger> pointLedgers);
}
