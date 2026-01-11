package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;

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
            PointAmount usedAmount
    ) {}

    public UsageResult use(List<PointLedger> availableLedgers, PointAmount amount) {
        List<PointLedger> updatedLedgers = new ArrayList<>();
        List<UsageDetail> usageDetails = new ArrayList<>();

        PointAmount remainingAmount = amount;

        for (PointLedger ledger : availableLedgers) {
            if (remainingAmount.isZero()) {
                break;
            }

            PointAmount usedFromLedger = ledger.use(remainingAmount);
            remainingAmount = remainingAmount.subtract(usedFromLedger);

            updatedLedgers.add(ledger);
            usageDetails.add(new UsageDetail(ledger.getId(), usedFromLedger));
        }

        return new UsageResult(updatedLedgers, usageDetails);
    }
}
