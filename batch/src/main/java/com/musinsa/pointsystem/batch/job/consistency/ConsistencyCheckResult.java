package com.musinsa.pointsystem.batch.job.consistency;

import java.util.UUID;

/**
 * 정합성 검증 결과
 */
public record ConsistencyCheckResult(
        UUID ledgerId,
        UUID memberId,
        boolean isConsistent,
        InconsistencyType type,
        String details
) {
    public enum InconsistencyType {
        NONE,
        AVAILABLE_AMOUNT_MISMATCH,
        USED_AMOUNT_MISMATCH,
        EARNED_AMOUNT_MISMATCH,
        MULTIPLE_MISMATCHES
    }

    /**
     * 정합성 일치하는 결과 생성
     */
    public static ConsistencyCheckResult consistent(UUID ledgerId, UUID memberId) {
        return new ConsistencyCheckResult(ledgerId, memberId, true, InconsistencyType.NONE, null);
    }

    /**
     * 정합성 불일치 결과 생성
     */
    public static ConsistencyCheckResult inconsistent(
            UUID ledgerId,
            UUID memberId,
            InconsistencyType type,
            String details
    ) {
        return new ConsistencyCheckResult(ledgerId, memberId, false, type, details);
    }
}
