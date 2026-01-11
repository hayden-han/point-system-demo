package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointUsageDetail;

import java.util.List;
import java.util.UUID;

public interface PointUsageDetailRepository {
    PointUsageDetail save(PointUsageDetail pointUsageDetail);
    List<PointUsageDetail> saveAll(List<PointUsageDetail> pointUsageDetails);
    List<PointUsageDetail> findByTransactionId(UUID transactionId);
    List<PointUsageDetail> findCancelableByTransactionId(UUID transactionId);
}
