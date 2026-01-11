package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.model.PointUsageDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PointUsagePolicy {

    public record UsageResult(
            List<PointLedger> updatedLedgers,
            List<UsageDetail> usageDetails
    ) {}

    public record UsageDetail(
            UUID ledgerId,
            Long usedAmount
    ) {}

    public UsageResult use(List<PointLedger> availableLedgers, Long amount) {
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<UsageDetail> usageDetails = new ArrayList<>();

        Long remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount <= 0) {
                break;
            }

            Long usedFromLedger = ledger.use(remainingAmount);
            remainingAmount -= usedFromLedger;

            updatedLedgers.add(ledger);
            usageDetails.add(new UsageDetail(ledger.getId(), usedFromLedger));
        }

        return new UsageResult(updatedLedgers, usageDetails);
    }
}
