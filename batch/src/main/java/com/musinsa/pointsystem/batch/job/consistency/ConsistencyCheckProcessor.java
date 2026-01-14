package com.musinsa.pointsystem.batch.job.consistency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Ledger 정합성 검증 Processor
 * - Ledger의 available_amount, used_amount가 Entry 기반 계산값과 일치하는지 검증
 */
@Slf4j
@Component
public class ConsistencyCheckProcessor implements ItemProcessor<LedgerConsistencyDto, ConsistencyCheckResult> {

    @Override
    public ConsistencyCheckResult process(LedgerConsistencyDto item) {
        List<String> mismatches = new ArrayList<>();

        // 1. available_amount 검증
        long expectedAvailable = item.calculatedAvailableAmount();
        if (item.availableAmount() != expectedAvailable) {
            mismatches.add(String.format(
                    "availableAmount: stored=%d, calculated=%d",
                    item.availableAmount(), expectedAvailable
            ));
        }

        // 2. used_amount 검증
        long expectedUsed = item.calculatedUsedAmount();
        if (item.usedAmount() != expectedUsed) {
            mismatches.add(String.format(
                    "usedAmount: stored=%d, calculated=%d",
                    item.usedAmount(), expectedUsed
            ));
        }

        // 3. earned_amount 검증 (EARN Entry와 일치하는지)
        // 취소된 경우 earnedAmount는 원래 적립금액 유지
        if (!item.isCanceled()) {
            long expectedEarned = item.entryEarnSum();  // EARN Entry의 합계
            if (item.earnedAmount() != expectedEarned) {
                mismatches.add(String.format(
                        "earnedAmount: stored=%d, calculated=%d",
                        item.earnedAmount(), expectedEarned
                ));
            }
        }

        if (mismatches.isEmpty()) {
            return ConsistencyCheckResult.consistent(item.ledgerId(), item.memberUuid());
        }

        ConsistencyCheckResult.InconsistencyType type = determineInconsistencyType(mismatches);
        String details = String.join("; ", mismatches);

        log.warn("Inconsistency detected - ledgerId={}, memberId={}, details={}",
                item.ledgerId(), item.memberUuid(), details);

        return ConsistencyCheckResult.inconsistent(
                item.ledgerId(),
                item.memberUuid(),
                type,
                details
        );
    }

    private ConsistencyCheckResult.InconsistencyType determineInconsistencyType(List<String> mismatches) {
        if (mismatches.size() > 1) {
            return ConsistencyCheckResult.InconsistencyType.MULTIPLE_MISMATCHES;
        }

        String mismatch = mismatches.get(0);
        if (mismatch.contains("availableAmount")) {
            return ConsistencyCheckResult.InconsistencyType.AVAILABLE_AMOUNT_MISMATCH;
        } else if (mismatch.contains("usedAmount")) {
            return ConsistencyCheckResult.InconsistencyType.USED_AMOUNT_MISMATCH;
        } else {
            return ConsistencyCheckResult.InconsistencyType.EARNED_AMOUNT_MISMATCH;
        }
    }
}
