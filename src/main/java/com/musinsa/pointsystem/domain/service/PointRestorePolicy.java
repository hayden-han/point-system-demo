package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 포인트 복구 정책 도메인 서비스
 * 사용취소 시 만료 여부에 따라 복구 또는 신규 적립 처리
 */
public class PointRestorePolicy {

    /**
     * 사용취소 결과
     */
    public record RestoreResult(
            List<PointLedger> restoredLedgers,
            List<NewLedgerInfo> newLedgers,
            List<PointUsageDetail> updatedUsageDetails
    ) {}

    /**
     * 신규 적립건 생성 정보
     */
    public record NewLedgerInfo(
            UUID memberId,
            Long amount,
            EarnType earnType,
            LocalDateTime expiredAt,
            UUID relatedTransactionId
    ) {}

    /**
     * 사용취소 시 적립건 복구 또는 신규 적립 처리
     *
     * @param usageDetails 취소 대상 사용 상세 목록 (만료일 긴 것부터 정렬됨)
     * @param ledgerMap 적립건 ID → 적립건 매핑
     * @param cancelAmount 취소할 금액
     * @param defaultExpirationDays 신규 적립 시 기본 만료일
     * @param cancelTransactionId 취소 트랜잭션 ID
     */
    public RestoreResult restore(
            List<PointUsageDetail> usageDetails,
            java.util.Map<UUID, PointLedger> ledgerMap,
            Long cancelAmount,
            int defaultExpirationDays,
            UUID cancelTransactionId,
            UUID memberId
    ) {
        List<PointLedger> restoredLedgers = new ArrayList<>();
        List<NewLedgerInfo> newLedgers = new ArrayList<>();
        List<PointUsageDetail> updatedUsageDetails = new ArrayList<>();

        Long remainingCancelAmount = cancelAmount;

        for (PointUsageDetail usageDetail : usageDetails) {
            if (remainingCancelAmount <= 0) {
                break;
            }

            Long cancelFromDetail = usageDetail.cancel(remainingCancelAmount);
            remainingCancelAmount -= cancelFromDetail;
            updatedUsageDetails.add(usageDetail);

            PointLedger originalLedger = ledgerMap.get(usageDetail.getLedgerId());

            if (originalLedger.isExpired()) {
                // 만료된 적립건 → 신규 적립 정보 생성
                newLedgers.add(new NewLedgerInfo(
                        memberId,
                        cancelFromDetail,
                        originalLedger.getEarnType(),
                        LocalDateTime.now().plusDays(defaultExpirationDays),
                        cancelTransactionId
                ));
            } else {
                // 만료되지 않은 적립건 → 복구
                originalLedger.restore(cancelFromDetail);
                restoredLedgers.add(originalLedger);
            }
        }

        return new RestoreResult(restoredLedgers, newLedgers, updatedUsageDetails);
    }
}
