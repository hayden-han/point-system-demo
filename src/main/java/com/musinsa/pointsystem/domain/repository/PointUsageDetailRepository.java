package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.PointUsageDetail;

import java.util.List;

public interface PointUsageDetailRepository {
    PointUsageDetail save(PointUsageDetail pointUsageDetail);
    List<PointUsageDetail> saveAll(List<PointUsageDetail> pointUsageDetails);
    List<PointUsageDetail> findByTransactionId(Long transactionId);
    List<PointUsageDetail> findCancelableByTransactionId(Long transactionId);
}
